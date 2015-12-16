/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import gololang.FunctionReference;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.genericMethodType;

public final class ClosureReferenceSupport {

  private ClosureReferenceSupport() {
    throw new UnsupportedOperationException("utility class");
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, String moduleClass, int arity, int varargs) throws Throwable {
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
