/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.runtime.OperatorType;

public final class MethodInvocation extends AbstractInvocation<MethodInvocation> {

  private boolean nullSafeGuarded = false;

  MethodInvocation(String name) {
    super(name);
  }

  protected MethodInvocation self() { return this; }

  public void setNullSafeGuarded(boolean nullSafeGuarded) {
    this.nullSafeGuarded = nullSafeGuarded;
  }

  public boolean isNullSafeGuarded() {
    return nullSafeGuarded;
  }

  public MethodInvocation nullSafe(boolean v) {
    setNullSafeGuarded(v);
    return this;
  }

  public MethodInvocation nullSafe() {
    return nullSafe(true);
  }

  public BinaryOperation on(Object target) {
    return Builders.binaryOperation(OperatorType.METHOD_CALL)
      .left(ExpressionStatement.of(target))
      .right(this);
  }

  @Override
  public MethodInvocation withArgs(Object... args) {
    super.withArgs(args);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMethodInvocation(this);
  }
}
