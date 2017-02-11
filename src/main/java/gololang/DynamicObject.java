/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;

import static java.lang.System.arraycopy;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static gololang.Predefined.isClosure;

/**
 * A dynamic object is an object whose properties can be dynamically added, changed and removed. Properties can be any
 * object value or a function reference.
 * <p>
 * The methods <code>plug</code> and <code>propertyMissing</code> are left undocumented. They are being used
 * by the Golo runtime to dispatch method invocations on dynamic objects.
 */
public final class DynamicObject {

  private final Object kind;
  private final HashMap<String, Object> properties = new HashMap<>();
  private boolean frozen = false;


  public DynamicObject() {
    this("DynamicObject");
  }

  public DynamicObject(Object kind) {
    this.kind = kind;
  }

  public boolean hasKind(Object k) {
    return Objects.equals(kind, k);
  }

  public boolean sameKind(DynamicObject other) {
    return Objects.equals(kind, other.kind);
  }

  @Override
  public String toString() {
    List<String> props = new LinkedList<>();
    for (Map.Entry<String, Object> prop : properties.entrySet()) {
      if (!isClosure(prop.getValue())) {
        props.add(String.format("%s=%s", prop.getKey(), prop.getValue().toString()));
      }
    }
    return String.format("%s{%s}", kind, String.join(", ", props));
  }

  /**
   * Defines a property.
   *
   * @param name  the property name.
   * @param value the property value.
   * @return the same dynamic object.
   * @throws IllegalStateException if the dynamic object is frozen.
   */
  public DynamicObject define(String name, Object value) {
    frozenMutationCheck();
    properties.put(name, value);
    return this;
  }

  /**
   * @return a view of all properties.
   */
  public Set<Map.Entry<String, Object>> properties() {
    return properties.entrySet();
  }

  /**
   * @param name the property name.
   * @return the property value.
   */
  public Object get(String name) {
    return properties.get(name);
  }

  /**
   * Removes a property.
   *
   * @param name the property name.
   * @return the same dynamic object.
   */
  public DynamicObject undefine(String name) {
    properties.remove(name);
    return this;
  }

  /**
   * @return a new dynamic object whose properties point to the same objects.
   */
  public DynamicObject copy() {
    DynamicObject copy = new DynamicObject(this.kind);
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      copy.properties.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  /**
   * Mixes all properties from another dynamic object into this one, overwriting existing properties.
   *
   * @param other the dynamic object to mix the properties from.
   * @return the same dynamic object.
   */
  public DynamicObject mixin(DynamicObject other) {
    frozenMutationCheck();
    for (Map.Entry<String, Object> entry : other.properties.entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Freezes a dynamic object, meaning that its properties cannot be added, updated and removed anymore.
   *
   * @return the same dynamic object.
   */
  public DynamicObject freeze() {
    this.frozen = true;
    return this;
  }

  /**
   * Tells whether the dynamic object is frozen or not.
   *
   * @return {@code true} if frozen, {@code false} otherwise.
   */
  public boolean isFrozen() {
    return frozen;
  }

  /**
   * Dispatch dynamic object "methods". The receiver dynamic object is expected to be the first element of {@code args}.
   *
   * @param property the method property in the dynamic object.
   * @param args     the arguments.
   * @return the return value.
   * @throws Throwable in case everything is wrong.
   */
  public static Object dispatchCall(String property, Object... args) throws Throwable {
    DynamicObject obj = (DynamicObject) args[0];
    Object value = obj.properties.get(property);
    if (value != null) {
      if (value instanceof FunctionReference) {
        FunctionReference funRef = (FunctionReference) value;
        if (funRef.isVarargsCollector() && args[args.length - 1] instanceof Object[]) {
          return funRef.spread(args);
        }
        return funRef.invoke(args);
      } else {
        throw new UnsupportedOperationException("There is no dynamic object method defined for " + property);
      }
    }
    if (obj.hasFallback()) {
      FunctionReference handle = (FunctionReference) obj.properties.get("fallback");
      Object[] fallback_args = new Object[args.length + 1];
      fallback_args[0] = obj;
      fallback_args[1] = property;
      arraycopy(args, 1, fallback_args, 2, args.length - 1);
      return handle.invoke(fallback_args);
    }
    throw new UnsupportedOperationException("There is neither a dynamic object method defined for " + property + " nor a 'fallback' method");
  }

  /**
   * Dispatches getter-style dynamic object methods, i.e., methods with a receiver and no argument.
   *
   * @param property the method property in the dynamic object.
   * @param object   the receiver object.
   * @return the return value.
   * @throws Throwable in case everything is wrong.
   */
  public static Object dispatchGetterStyle(String property, DynamicObject object) throws Throwable {
    Object value = object.get(property);
    if (value != null || object.properties.containsKey(property)) {
      if (value instanceof FunctionReference) {
        FunctionReference funRef = (FunctionReference) value;
        if (funRef.acceptArity(1)) {
          return funRef.invoke(object);
        }
      }
      return value;
    }
    if (object.hasFallback()) {
      FunctionReference funRef = (FunctionReference) object.properties.get("fallback");
      return funRef.invoke(object, property);
    }
    return null;
  }

  /**
   * Dispatches setter-style dynamic object methods, i.e., methods with a receiver and exactly 1 argument.
   *
   * @param property the method property in the dynamic object.
   * @param object   the receiver object.
   * @param arg      the arguments.
   * @return the return value.
   * @throws Throwable in case everything is wrong.
   */
  public static Object dispatchSetterStyle(String property, DynamicObject object, Object arg) throws Throwable {
    Object value = object.get(property);
    if (value != null || object.properties.containsKey(property)) {
      if (value instanceof FunctionReference) {
        FunctionReference funRef = (FunctionReference) value;
        if (funRef.arity() == 2) {
          if (funRef.isVarargsCollector() && arg instanceof Object[]) {
            return funRef.handle().invokeExact((Object) object, (Object[]) arg);
          }
          return funRef.invoke(object, arg);
        }
      }
    }
    // XXX: should we try the fallback method here ?
    return object.define(property, arg);
  }

  /**
   * Dispatches on another dynamic object (fallback helper).
   *
   * @param deleguee the object to delegate to.
   * @param receiver the receiver object.
   * @param property the method property in the dynamic object.
   * @param args     the arguments.
   * @return the return value.
   * @throws Throwable in case everything is wrong.
   */
  public static Object dispatchDelegate(DynamicObject deleguee, DynamicObject receiver, String property, Object... args) throws Throwable {
    return deleguee
      .invoker(property, genericMethodType(args.length + 1))
      .bindTo(deleguee)
      .invokeWithArguments(args);
  }

  /**
   * Creates a function suitable for the {@fallback} property delegating to the given dynamic object.
   *
   * Example:
   * <code><pre>
   * let d = DynamicObject(): name("Zaphod")
   * let o = DynamicObject(): fallback(delegate(d))
   * </pre></code>
   *
   * @param deleguee the object to delegate to.
   * @return a function delegating to {@code deleguee}
   */
  public static FunctionReference delegate(DynamicObject deleguee) {
    return new FunctionReference(
        DISPATCH_DELEGATE.bindTo(deleguee).asVarargsCollector(Object[].class), //.asType(genericMethodType(2, true)),
        new String[]{"this", "name", "args"});
  }

  /**
   * Gives an invoker method handle for a given property.
   * <p>
   * While this method may be useful in itself, it is mostly relevant for the Golo runtime internals so as
   * to allow calling "methods" on dynamic objects, as in:
   * <pre>
   * # obj is some dynamic object...
   * obj: foo("bar")
   * println(foo: bar())
   *
   * obj: define("plop", |this| -> "Plop!")
   * println(obj: plop())
   * </pre>
   *
   * @param property the name of a property.
   * @param type     the expected invoker type with at least one parameter (the dynamic object as a receiver).
   * @return a method handle.
   */
  public MethodHandle invoker(String property, MethodType type) {
    switch (type.parameterCount()) {
      case 0:
        throw new IllegalArgumentException("A dynamic object invoker type needs at least 1 argument (the receiver)");
      case 1:
        return DISPATCH_GET.bindTo(property).asType(genericMethodType(1));
      case 2:
        return DISPATCH_SET.bindTo(property).asType(genericMethodType(2));
      default:
        return DISPATCH_CALL.bindTo(property).asCollector(Object[].class, type.parameterCount());
    }
  }

  /**
   * Verify if a method is defined for the dynamic object.
   *
   * @param method the method name.
   * @return {@code true} if method is defined, {@code false} otherwise.
   */
  public boolean hasMethod(String method) {
    Object obj = properties.get(method);
    if (obj != null) {
      return isClosure(obj);
    }
    return false;
  }

  /**
   * Let the user define a fallback behavior.
   *
   * @param value the fallback value
   * @return the current object
   */
  public DynamicObject fallback(Object value) {
    return define("fallback", value);
  }

  /**
   * Verify a fallback property exists.
   *
   * @return {@code true} if a fallback behavior is defined, {@code false} otherwise.
   */
  private boolean hasFallback() {
    return properties.containsKey("fallback");
  }

  public static final MethodHandle DISPATCH_CALL;
  public static final MethodHandle DISPATCH_GET;
  public static final MethodHandle DISPATCH_SET;
  public static final MethodHandle DISPATCH_DELEGATE;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      DISPATCH_DELEGATE = lookup.findStatic(DynamicObject.class, "dispatchDelegate",
          methodType(Object.class, DynamicObject.class, DynamicObject.class, String.class, Object[].class));
      DISPATCH_CALL = lookup.findStatic(DynamicObject.class, "dispatchCall", methodType(Object.class, String.class, Object[].class));
      DISPATCH_GET = lookup.findStatic(DynamicObject.class, "dispatchGetterStyle", methodType(Object.class, String.class, DynamicObject.class));
      DISPATCH_SET = lookup.findStatic(DynamicObject.class, "dispatchSetterStyle", methodType(Object.class, String.class, DynamicObject.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
      throw new Error("Could not bootstrap the required method handles");
    }
  }

  private void frozenMutationCheck() {
    if (frozen) {
      throw new IllegalStateException("the object is frozen");
    }
  }
}
