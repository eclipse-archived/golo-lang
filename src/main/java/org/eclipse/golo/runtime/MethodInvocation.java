/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.*;

/**
 * Encapsulate informations about a runtime method call.
 */
public class MethodInvocation {
  public final String name;
  public final Class<?> receiverClass;
  public final Object[] arguments;
  public final int arity;
  public final String[] argumentNames;
  public final MethodType type;

  MethodInvocation(String name, MethodType type, Object[] args, String[] argNames) {
    this.name = name;
    this.receiverClass = args[0].getClass();
    this.arguments = args;
    this.arity = args.length;
    this.type = type;
    this.argumentNames = new String[argNames.length + 1];
    this.argumentNames[0] = "this";
    System.arraycopy(argNames, 0, argumentNames, 1, argNames.length);
  }

  private boolean isLastArgumentAnArray() {
    return arity > 0
      && arguments.length == arity
      && arguments[arity - 1] instanceof Object[];
  }

  public MethodHandle coerce(MethodHandle target) {
    if (target.isVarargsCollector() && isLastArgumentAnArray()) {
      return target.asFixedArity().asType(type);
    }
    return target.asType(type);
  }

  public boolean match(Method method) {
    return method.getName().equals(name)
      && isPublic(method.getModifiers())
      && !isAbstract(method.getModifiers())
      && TypeMatching.argumentsMatch(method, arguments);
  }

  @Override
  public String toString() {
    return name + type + java.util.Arrays.asList(arguments);
  }


}
