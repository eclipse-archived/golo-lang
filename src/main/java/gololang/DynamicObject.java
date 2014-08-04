/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gololang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static fr.insalyon.citi.golo.runtime.TypeMatching.isLastArgumentAnArray;
import static java.lang.System.arraycopy;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;

/**
 * A dynamic object is an object whose properties can be dynamically added, changed and removed. Properties can be any
 * object value or a method handle to a closure.
 * <p>
 * The methods <code>plug</code> and <code>propertyMissing</code> are left undocumented. They are being used
 * by the Golo runtime to dispatch method invocations on dynamic objects.
 */
public final class DynamicObject {

  private final HashMap<String, Object> properties = new HashMap<>();
  private boolean frozen = false;

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
    DynamicObject copy = new DynamicObject();
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
      if (value instanceof MethodHandle) {
        MethodHandle handle = (MethodHandle) value;
        if (handle.isVarargsCollector() && args[args.length - 1] instanceof Object[]) {
          Object[] trailing = (Object[]) args[args.length - 1];
          Object[] spreadArgs = new Object[args.length + trailing.length - 1];
          arraycopy(args, 0, spreadArgs, 0, args.length - 1);
          arraycopy(trailing, 0, spreadArgs, args.length - 1, trailing.length);
          return handle.invokeWithArguments(spreadArgs);
        }
        return handle.invokeWithArguments(args);
      } else {
        throw new UnsupportedOperationException("There is no dynamic object method defined for " + property);
      }
    }
    if (obj.hasFallback()) {
      MethodHandle handle = (MethodHandle) obj.properties.get("fallback");
      Object[] fallback_args = new Object[args.length + 1];
      fallback_args[0] = obj;
      fallback_args[1] = property;
      arraycopy(args, 1, fallback_args, 2, args.length - 1);
      return handle.invokeWithArguments(fallback_args);
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
      if (value instanceof MethodHandle) {
        MethodHandle handle = (MethodHandle) value;
        if (handle.type().parameterCount() == 1 || handle.isVarargsCollector()) {
          return handle.invokeWithArguments(object);
        }
      }
      return value;
    }
    if (object.hasFallback()) {
      MethodHandle handle = (MethodHandle) object.properties.get("fallback");
      return handle.invokeWithArguments(object, property);
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
      if (value instanceof MethodHandle) {
        MethodHandle handle = (MethodHandle) value;
        if (handle.type().parameterCount() == 2) {
          if (handle.isVarargsCollector() && arg instanceof Object[]) {
            return handle.invokeExact((Object) object, (Object[]) arg);
          }
          return handle.invokeWithArguments(object, arg);
        }
      }
    }
    return object.define(property, arg);
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
      return (obj instanceof MethodHandle);
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

  private static final MethodHandle MAP_GET;
  private static final MethodHandle MAP_PUT;
  private static final MethodHandle MAP_HAS;
  private static final MethodHandle IS_MH_1;
  private static final MethodHandle IS_MH_2;
  private static final MethodHandle VARARGS_COMBINER;

  public static final MethodHandle DISPATCH_CALL;
  public static final MethodHandle DISPATCH_GET;
  public static final MethodHandle DISPATCH_SET;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {

      DISPATCH_CALL = lookup.findStatic(DynamicObject.class, "dispatchCall", methodType(Object.class, String.class, Object[].class));
      DISPATCH_GET = lookup.findStatic(DynamicObject.class, "dispatchGetterStyle", methodType(Object.class, String.class, DynamicObject.class));
      DISPATCH_SET = lookup.findStatic(DynamicObject.class, "dispatchSetterStyle", methodType(Object.class, String.class, DynamicObject.class, Object.class));

      MAP_GET = lookup.findSpecial(DynamicObject.class, "get", methodType(Object.class, Object.class), DynamicObject.class);
      MAP_PUT = lookup.findSpecial(DynamicObject.class, "put", methodType(Object.class, String.class, Object.class), DynamicObject.class);
      MAP_HAS = lookup.findStatic(DynamicObject.class, "has", methodType(boolean.class, Object.class, Object.class));
      IS_MH_1 = lookup.findStatic(DynamicObject.class, "isMethodHandle_1", methodType(boolean.class, Object.class));
      IS_MH_2 = lookup.findStatic(DynamicObject.class, "isMethodHandle_2", methodType(boolean.class, Object.class));
      VARARGS_COMBINER = lookup.findStatic(DynamicObject.class, "varargsCombiner", methodType(Object.class, Object.class, Object[].class));

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

  private Object put(String key, Object value) {
    frozenMutationCheck();
    properties.put(key, value);
    return this;
  }

  private Object get(Object key) {
    return properties.get(key);
  }

  private MethodHandle anyInvoker(String property, MethodType type) {
    MethodHandle mapGet = insertArguments(MAP_GET, 1, property);
    mapGet = mapGet.asType(mapGet.type().changeParameterType(0, Object.class));
    MethodHandle vaCombiner = VARARGS_COMBINER.asCollector(Object[].class, type.parameterCount());
    MethodHandle mapGetCombined = foldArguments(vaCombiner, mapGet);
    MethodHandle invoker = MethodHandles.invoker(type);
    invoker = invoker.asType(invoker.type().changeParameterType(0, Object.class));
    MethodHandle combined = foldArguments(invoker, mapGetCombined);
    if (hasFallback()) {
      return fallback(combined, property, type);
    }
    return combined;
  }

  private MethodHandle setterStyleInvoker(String property, MethodType type) {
    MethodHandle mapGet = insertArguments(MAP_GET, 1, property);
    mapGet = mapGet.asType(mapGet.type().changeParameterType(0, Object.class));
    MethodHandle mapPut = dropArguments(insertArguments(MAP_PUT, 1, property), 0, Object.class);
    mapPut = mapPut.asType(mapPut.type().changeParameterType(1, Object.class));
    MethodHandle vaCombiner = VARARGS_COMBINER.asCollector(Object[].class, type.parameterCount());
    MethodHandle mapGetCombined = foldArguments(vaCombiner, mapGet);
    MethodHandle invoker = MethodHandles.invoker(type);
    invoker = invoker.asType(invoker.type().changeParameterType(0, Object.class));
    MethodHandle gwt = guardWithTest(IS_MH_2, invoker, mapPut);
    return foldArguments(gwt, mapGetCombined);
  }

  private MethodHandle getterStyleInvoker(String property, MethodType type) {
    MethodHandle mapGet = insertArguments(MAP_GET, 1, property);
    mapGet = mapGet.asType(mapGet.type().changeParameterType(0, Object.class));
    MethodHandle vaCombiner = VARARGS_COMBINER.asCollector(Object[].class, type.parameterCount());
    MethodHandle mapGetCombined = foldArguments(vaCombiner, mapGet);
    MethodHandle identity = dropArguments(identity(Object.class), 1, type.parameterArray());
    MethodHandle invoker = MethodHandles.invoker(type);
    invoker = invoker.asType(invoker.type().changeParameterType(0, Object.class));
    MethodHandle gwt = guardWithTest(IS_MH_1, invoker, identity);
    if (hasFallback()) {
      return fallback(foldArguments(gwt, mapGetCombined), property, type);
    }
    return foldArguments(gwt, mapGetCombined);
  }

  private MethodHandle fallback(MethodHandle target, String property, MethodType type) {
    MethodHandle fallbackHandle = (MethodHandle) properties.get("fallback");
    fallbackHandle = insertArguments(fallbackHandle, 1, property);
    fallbackHandle = fallbackHandle.asType(fallbackHandle.type().changeParameterType(0, Object.class));
    fallbackHandle = fallbackHandle.asCollector(Object[].class, target.type().parameterCount() - 1);
    MethodHandle has = insertArguments(MAP_HAS, 1, property);
    return guardWithTest(has, target, fallbackHandle);
  }

  private static Object varargsCombiner(Object mapEntry, Object... args) {
    if (mapEntry == null || !(mapEntry instanceof MethodHandle)) {
      return mapEntry;
    }
    MethodHandle target = (MethodHandle) mapEntry;
    if (target.isVarargsCollector() && isLastArgumentAnArray(target.type().parameterCount(), args)) {
      return target.asFixedArity();
    }
    return target;
  }

  private static boolean isMethodHandle_1(Object obj) {
    if (obj instanceof MethodHandle) {
      MethodHandle handle = (MethodHandle) obj;
      if (handle.isVarargsCollector()) {
        return handle.type().parameterCount() == 2;
      }
      return handle.type().parameterCount() == 1;
    }
    return false;
  }

  private static boolean isMethodHandle_2(Object obj) {
    if (obj instanceof MethodHandle) {
      MethodHandle handle = (MethodHandle) obj;
      return handle.type().parameterCount() == 2;
    }
    return false;
  }

  private static boolean has(Object obj, Object property) {
    if (obj instanceof DynamicObject) {
      DynamicObject receiver = (DynamicObject) obj;
      return receiver.properties.containsKey(property);
    }
    return false;
  }

}
