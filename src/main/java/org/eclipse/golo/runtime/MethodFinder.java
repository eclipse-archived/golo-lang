/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.List;
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;

import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodHandles.Lookup;
import static org.eclipse.golo.runtime.NamedArgumentsHelper.hasNamedParameters;
import static org.eclipse.golo.runtime.NamedArgumentsHelper.getParameterNames;
import static org.eclipse.golo.runtime.NamedArgumentsHelper.checkArgumentPosition;

abstract class MethodFinder {

  protected final MethodInvocation invocation;
  protected final Lookup lookup;
  protected final Class<?> callerClass;

  MethodFinder(MethodInvocation invocation, Lookup lookup) {
    this.invocation = invocation;
    this.lookup = lookup;
    this.callerClass = lookup.lookupClass();
  }

  public abstract MethodHandle find();

  protected int[] getArgumentsOrder(Method method, List<String> parameterNames, String[] argumentNames) {
    // deal with the first parameter (implicit receiver).
    int[] argumentsOrder = new int[parameterNames.size() + 1];
    argumentsOrder[0] = 0;
    for (int i = 0; i < argumentNames.length; i++) {
      int actualPosition = parameterNames.indexOf(argumentNames[i]);
      checkArgumentPosition(actualPosition, argumentNames[i], method.getName() + parameterNames);
      argumentsOrder[actualPosition + 1] = i + 1;
    }
    return argumentsOrder;
  }

  public MethodHandle reorderArguments(Method method, MethodHandle handle) {
    String[] argumentNames = invocation.argumentNames();
    if (argumentNames.length == 0) { return handle; }
    if (hasNamedParameters(method)) {
      return permuteArguments(handle, handle.type(), getArgumentsOrder(method, getParameterNames(method), argumentNames));
    }
    Warnings.noParameterNames(method.getName(), argumentNames);
    return handle;
  }

  protected Optional<MethodHandle> toMethodHandle(Method method) {
    MethodHandle target = null;
    if (isMethodDecorated(method)) {
      target = getDecoratedMethodHandle(lookup, method, invocation.arity());
    } else {
      try {
        target = lookup.unreflect(method);
      } catch (IllegalAccessException e) {
        /* We need to give augmentations a chance, as IllegalAccessException can be noise in our resolution.
         * Example: augmenting HashSet with a map function.
         *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
         */
        return Optional.empty();
      }
    }
    return Optional.of(invocation.coerce(reorderArguments(method, target)));
  }

}
