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

import java.lang.invoke.MethodHandle;
import gololang.ir.AbstractInvocation;
import gololang.ir.GoloElement;

final class MacroFinderResult {
  private final boolean special;
  private final boolean contextual;
  private final MethodHandle target;

  MacroFinderResult(MethodHandle target, boolean special, boolean contextual) {
    this.special = special;
    this.contextual = contextual;
    this.target = target;
  }

  MethodHandle binded(MacroExpansionIrVisitor visitor, AbstractInvocation<?> invocation) {
    MethodHandle handle = this.target;
    if (this.contextual) {
      handle = handle.bindTo(invocation);
    }
    if (this.special) {
      handle = handle.bindTo(visitor);
    }
    if (this.target.isVarargsCollector()) {
      handle = handle.asVarargsCollector(GoloElement[].class);
    }
    return handle;
  }

  @Override
  public String toString() {
    return String.format("MethodFinderResult{special=%s,contextual=%s,target=%s}", special, contextual, target);
  }
}

