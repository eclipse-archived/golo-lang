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

package fr.insalyon.citi.golo.runtime.adapters;

import java.lang.invoke.*;
import java.util.Map;

import static java.lang.invoke.MethodType.genericMethodType;

public class AdapterSupport {

  public static final String DEFINITION_FIELD = "_$_$adapter_$definition";

  private static final MethodHandle FALLBACK;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findStatic(AdapterSupport.class, "fallback", MethodType.methodType(Object.class, AdapterCallSite.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  static final class AdapterCallSite extends MutableCallSite {

    final MethodHandles.Lookup callerLookup;
    final String name;

    AdapterCallSite(MethodType type, MethodHandles.Lookup callerLookup, String name) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
    AdapterCallSite callSite = new AdapterCallSite(type, caller, name);
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static Object fallback(AdapterCallSite callSite, Object[] args) throws Throwable {
    Class<?> receiverClass = args[0].getClass();
    Class<?> receiverParentClass = receiverClass.getSuperclass();
    AdapterDefinition definition = (AdapterDefinition) receiverClass.getField(DEFINITION_FIELD).get(args[0]);
    Map<String, MethodHandle> implementations = definition.getImplementations();
    MethodHandle target = implementations.get(callSite.name);
    if (target == null) {
      target = implementations.get("*");
      if (target != null) {
        target = target.bindTo(callSite.name).asCollector(Object[].class, args.length);
      }
    }
    if (target == null) {
      Map<String, MethodHandle> overrides = definition.getOverrides();
      MethodHandle superTarget = callSite.callerLookup.findSpecial(receiverParentClass, callSite.name, callSite.type().dropParameterTypes(0, 1), receiverClass);
      if (superTarget.isVarargsCollector()) {
        superTarget = superTarget.asType(genericMethodType(superTarget.type().parameterCount() - 1, true)).asVarargsCollector(Object[].class);
      } else {
        superTarget = superTarget.asType(genericMethodType(superTarget.type().parameterCount()));
      }
      target = overrides.get(callSite.name);
      boolean star = false;
      if (target == null) {
        target = overrides.get("*");
        star = true;
      }
      if (target == null) {
        target = superTarget;
      } else {
        target = target.bindTo(superTarget);
        if (star) {
          target = target.bindTo(callSite.name);
          target = target.asCollector(Object[].class, args.length);
        }
      }
    }
    callSite.setTarget(target.asType(callSite.type()));
    return target.invokeWithArguments(args);
  }
}
