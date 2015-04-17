/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime.adapters;

import gololang.FunctionReference;

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
    Map<String, FunctionReference> implementations = definition.getImplementations();
    MethodHandle target = null;
    if (implementations.containsKey(callSite.name)) {
      target = implementations.get(callSite.name).handle();
    }
    if (target == null) {
      if (implementations.containsKey("*")) {
        target = implementations.get("*").handle();
        target = target.bindTo(callSite.name).asCollector(Object[].class, args.length);
      }
    }
    if (target == null) {
      Map<String, FunctionReference> overrides = definition.getOverrides();
      MethodHandle superTarget = callSite.callerLookup.findSpecial(receiverParentClass, callSite.name, callSite.type().dropParameterTypes(0, 1), receiverClass);
      if (superTarget.isVarargsCollector()) {
        superTarget = superTarget.asType(genericMethodType(superTarget.type().parameterCount() - 1, true)).asVarargsCollector(Object[].class);
      } else {
        superTarget = superTarget.asType(genericMethodType(superTarget.type().parameterCount()));
      }
      if (overrides.containsKey(callSite.name)) {
        target = overrides.get(callSite.name).handle();
      }
      boolean star = false;
      if (target == null) {
        if (overrides.containsKey("*")) {
          target = overrides.get("*").handle();
        }
        star = true;
      }
      if (target == null) {
        target = superTarget;
      } else {
        target = target.bindTo(new FunctionReference(superTarget));
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
