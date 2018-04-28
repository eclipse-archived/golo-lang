/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import gololang.FunctionReference;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.genericMethodType;

public final class ClosureReferenceSupport {

  private ClosureReferenceSupport() {
    throw new UnsupportedOperationException("Don't instantiate invokedynamic bootstrap class");
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type, String moduleClass, int arity, int varargs) throws Throwable {
    Class<?> module = caller.lookupClass().getClassLoader().loadClass(moduleClass);
    Method function = module.getDeclaredMethod(name, genericMethodType(arity, varargs == 1).parameterArray());
    function.setAccessible(true);
    return new ConstantCallSite(constant(
          FunctionReference.class,
          new FunctionReference(caller.unreflect(function), parameterNames(function))));
  }

  private static String[] parameterNames(Method function) {
    Parameter[] parameters = function.getParameters();
    String[] parameterNames = new String[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      parameterNames[i] = parameters[i].getName();
    }
    return parameterNames;
  }

}
