/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.macro;

import gololang.ir.AbstractInvocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

class MacroCache {
  private final Map<MacroKey, MacroFinderResult> cache = new HashMap<>();

  private static final class MacroKey {
    private final String name;
    private final int arity;

    MacroKey(String name, int arity) {
      this.name = name;
      this.arity = arity;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) { return false; }
      if (o == this) { return true; }
      if (!(o instanceof MacroKey)) { return false; }
      MacroKey that = (MacroKey) o;
      return this.name.equals(that.name) && this.arity == that.arity;
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(name, arity);
    }

    public static MacroKey of(AbstractInvocation<?> invocation) {
      return new MacroKey(invocation.getName(), invocation.getArity());
    }

    @Override
    public String toString() {
      return String.format("MacroKey<%s,%s>", name, arity);
    }
  }

  public void clear() {
    cache.clear();
  }

  public Optional<MacroFinderResult> getOrCompute(AbstractInvocation<?> invocation, Function<AbstractInvocation<?>, Optional<MacroFinderResult>> finder) {
    if (invocation == null) {
      return Optional.empty();
    }
    MacroKey key = MacroKey.of(invocation);
    if (cache.containsKey(key)) {
      return Optional.of(cache.get(key));
    }
    Optional<MacroFinderResult> method = finder.apply(invocation);
    if (method.isPresent()) {
      cache.put(key, method.get());
    }
    return method;
  }
}
