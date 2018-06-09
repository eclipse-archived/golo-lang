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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;

import static java.lang.invoke.MethodHandles.Lookup;
import static org.eclipse.golo.runtime.NamedArgumentsHelper.getParameterNames;

abstract class MethodFinder {

  protected final MethodInvocation invocation;
  protected final Lookup lookup;
  protected final Class<?> callerClass;

  MethodFinder(MethodInvocation invocation, Lookup lookup) {
    this.invocation = invocation;
    this.lookup = lookup;
    this.callerClass = lookup.lookupClass();
  }

  abstract MethodHandle find();

  public MethodHandle reorderArguments(Method method, MethodHandle handle) {
    return NamedArgumentsHelper.reorderArguments(
        method.getName(),
        getParameterNames(method),
        handle,
        invocation.argumentNames(),
        1, 1);
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
