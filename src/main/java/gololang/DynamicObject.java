package gololang;

import java.lang.invoke.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

public class DynamicObject {

  private final Map<String, Set<SwitchPoint>> switchPoints = new HashMap<>();
  private final Map<String, Object> properties = new HashMap<>();

  public DynamicObject define(String name, Object value) {
    properties.put(name, value);
    if (switchPoints.containsKey(name)) {
      invalidate(name);
    } else {
      switchPoints.put(name, new HashSet<SwitchPoint>());
    }
    return this;
  }

  private void invalidate(String name) {
    Set<SwitchPoint> switches = switchPoints.get(name);
    SwitchPoint.invalidateAll(switches.toArray(new SwitchPoint[switches.size()]));
    switches.clear();
  }

  public Object get(String name) {
    return properties.get(name);
  }

  public DynamicObject undefine(String name) {
    if (properties.containsKey(name)) {
      properties.remove(name);
      invalidate(name);
      switchPoints.remove(name);
    }
    return this;
  }

  private static final MethodHandle FALLBACK;
  private static final MethodHandle PROPERTY_MISSING;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findStatic(DynamicObject.class, "fallback",
          methodType(Object.class, MutableCallSite.class, String.class, DynamicObject.class, Object[].class));
      PROPERTY_MISSING = lookup.findStatic(DynamicObject.class, "propertyMissing",
          methodType(Object.class, String.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles");
    }
  }

  public static Object fallback(MutableCallSite callSite, String name, DynamicObject dynamicObject, Object[] args) throws Throwable {
    dynamicObject.plug(callSite, name);
    return callSite.dynamicInvoker().invokeWithArguments(args);
  }

  public static Object propertyMissing(String name, Object[] args) throws NoSuchMethodException {
    throw new NoSuchMethodException("Missing DynamicObject definition for " + name);
  }

  public MutableCallSite plug(MutableCallSite callSite, String name) {
    MethodHandle target;
    MethodType type = callSite.type();
    Object value = properties.get(name);
    boolean isFunction = value instanceof MethodHandle;
    if (value != null) {
      if (isFunction) {
        target = (MethodHandle) value;
      } else {
        target = MethodHandles.constant(Object.class, value);
      }
    } else {
      target = PROPERTY_MISSING
          .bindTo(name)
          .asCollector(Object[].class, type.parameterCount())
          .asType(type);
      switchPoints.put(name, new HashSet<SwitchPoint>());
    }
    MethodHandle fallback = insertArguments(FALLBACK, 0, callSite, name, this)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    SwitchPoint switchPoint = new SwitchPoint();
    callSite.setTarget(switchPoint.guardWithTest(target.asType(type), fallback));
    switchPoints.get(name).add(switchPoint);
    return callSite;
  }
}
