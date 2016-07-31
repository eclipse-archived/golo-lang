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
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;

import static java.lang.invoke.MethodHandles.Lookup;

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
    if (invocation.argumentNames().length > 1) {
      target = FunctionCallSupport.reorderArguments(method, target, invocation.argumentNames());
    }
    return Optional.of(invocation.coerce(target));
  }

}
