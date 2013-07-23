/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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
import java.lang.invoke.SwitchPoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class DynamicObject {

  private final HashMap<String, Set<SwitchPoint>> switchPoints = new HashMap<>();
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
    if (frozen) {
      throw new IllegalStateException("the object is frozen");
    }
    properties.put(name, value);
    if (switchPoints.containsKey(name)) {
      invalidate(name);
    } else {
      switchPoints.put(name, new HashSet<SwitchPoint>());
    }
    return this;
  }

  /**
   * @return a view of all properties.
   */
  public Set<Map.Entry<String, Object>> properties() {
    return properties.entrySet();
  }

  private void invalidate(String name) {
    Set<SwitchPoint> switches = switchPoints.get(name);
    SwitchPoint.invalidateAll(switches.toArray(new SwitchPoint[switches.size()]));
    switches.clear();
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
    if (properties.containsKey(name)) {
      properties.remove(name);
      invalidate(name);
      switchPoints.remove(name);
    }
    return this;
  }

  /**
   * @return a new dynamic object whose properties point to the same objects.
   */
  public DynamicObject copy() {
    DynamicObject copy = new DynamicObject();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      define(entry.getKey(), entry.getValue());
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
    for (Map.Entry<String, Object> entry : other.properties.entrySet()) {
      define(entry.getKey(), entry.getValue());
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

  // New stuff
  private static final MethodHandle MAP_GET;
  private static final MethodHandle MAP_PUT;
  private static final MethodHandle IS_MH;

  // Old stuff
  private static final MethodHandle PROPERTY_MISSING;
  private static final MethodHandle DEFINE;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {

      // New stuff
      MAP_GET = lookup.findSpecial(DynamicObject.class, "get", methodType(Object.class, Object.class), DynamicObject.class);
      MAP_PUT = lookup.findSpecial(DynamicObject.class, "put", methodType(Object.class, String.class, Object.class), DynamicObject.class);
      IS_MH = lookup.findStatic(DynamicObject.class, "isMethodHandle", methodType(boolean.class, Object.class));

      // Old stuff
      PROPERTY_MISSING = lookup.findStatic(DynamicObject.class, "propertyMissing",
          methodType(Object.class, String.class, Object[].class));
      DEFINE = lookup.findVirtual(DynamicObject.class, "define",
          methodType(DynamicObject.class, String.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
      throw new Error("Could not bootstrap the required method handles");
    }
  }

  // New stuff


  private Object put(String key, Object value) {
    return properties.put(key, value);
  }

  private Object get(Object key) {
    return properties.get(key);
  }

  public MethodHandle invoker(String property, MethodType type) {
    switch (type.parameterCount()) {
      case 0:
        throw new IllegalArgumentException("A dynamic object invoker type needs at least 1 argument (the receiver)");
      case 1:
        return getterStyleInvoker(property, type);
      case 2:
        return setterStyleInvoker(property, type);
      default:
        return anyInvoker(property, type);
    }
  }

  private MethodHandle anyInvoker(String property, MethodType type) {
    MethodHandle get_mh = insertArguments(MAP_GET, 1, property);
    get_mh = get_mh.asType(get_mh.type().changeParameterType(0, Object.class));
    MethodHandle invoker_mh = exactInvoker(type);
    invoker_mh = invoker_mh.asType(invoker_mh.type().changeParameterType(0, Object.class));
    return foldArguments(invoker_mh, get_mh);
  }

  private MethodHandle setterStyleInvoker(String property, MethodType type) {
    MethodHandle get_mh = insertArguments(MAP_GET, 1, property);
    get_mh = get_mh.asType(get_mh.type().changeParameterType(0, Object.class));
    MethodHandle put_mh = dropArguments(insertArguments(MAP_PUT, 1, property), 0, Object.class);
    put_mh = put_mh.asType(put_mh.type().changeParameterType(1, Object.class));
    MethodHandle invoker_mh = exactInvoker(type);
    invoker_mh = invoker_mh.asType(invoker_mh.type().changeParameterType(0, Object.class));
    MethodHandle gwt_mh = guardWithTest(IS_MH, invoker_mh, put_mh);
    return foldArguments(gwt_mh, get_mh);
  }

  private MethodHandle getterStyleInvoker(String property, MethodType type) {
    MethodHandle get_mh = insertArguments(MAP_GET, 1, property);
    get_mh = get_mh.asType(get_mh.type().changeParameterType(0, Object.class));
    MethodHandle echo_mh = dropArguments(identity(Object.class), 1, type.parameterArray());
    MethodHandle invoker_mh = exactInvoker(type);
    invoker_mh = invoker_mh.asType(invoker_mh.type().changeParameterType(0, Object.class));
    MethodHandle gwt_mh = guardWithTest(IS_MH, invoker_mh, echo_mh);
    return foldArguments(gwt_mh, get_mh);
  }

  private static boolean isMethodHandle(Object obj) {
    return obj instanceof MethodHandle;
  }

  // Old stuff

  public static Object propertyMissing(String name, Object[] args) throws NoSuchMethodException {
    throw new NoSuchMethodException("Missing DynamicObject definition for " + name);
  }

  public MethodHandle plug(String name, MethodType type, MethodHandle fallback) {
    Object value = properties.get(name);
    MethodHandle target;
    int parameterCount = type.parameterCount();
    if (value != null) {
      if (value instanceof MethodHandle) {
        target = (MethodHandle) value;
        if (missesReceiverType(target)) {
          throw new IllegalArgumentException(name + " must have a first a non-array first argument as the dynamic object");
        }
        if (wrongMethodSignature(type, target)) {
          throw new IllegalArgumentException(name + " must have the following signature: " + type + " (found: " + target.type() + ")");
        }
      } else {
        if (parameterCount == 1) {
          target = dropArguments(constant(Object.class, value), 0, DynamicObject.class);
        } else if (parameterCount == 2) {
          target = insertArguments(DEFINE, 1, name);
        } else {
          throw new IllegalArgumentException(name + " needs to invoked with just 1 argument");
        }
      }
    } else {
      if (parameterCount == 2) {
        target = insertArguments(DEFINE, 1, name);
      } else {
        target = PROPERTY_MISSING
            .bindTo(name)
            .asCollector(Object[].class, parameterCount)
            .asType(type);
      }
      switchPoints.put(name, new HashSet<SwitchPoint>());
    }
    SwitchPoint switchPoint = new SwitchPoint();
    switchPoints.get(name).add(switchPoint);
    return switchPoint.guardWithTest(target.asType(type), fallback);
  }

  private boolean wrongMethodSignature(MethodType type, MethodHandle target) {
    return target.type().parameterCount() != type.parameterCount();
  }

  private boolean missesReceiverType(MethodHandle target) {
    return target.type().parameterCount() < 1 || target.type().parameterType(0).isArray();
  }
}
