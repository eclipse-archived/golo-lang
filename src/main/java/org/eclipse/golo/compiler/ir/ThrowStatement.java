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

import java.util.Objects;

public final class ThrowStatement extends GoloStatement<ThrowStatement> {

  private GoloStatement<?> expressionStatement;

  ThrowStatement(GoloStatement<?> expressionStatement) {
    super();
    setExpressionStatement(expressionStatement);
  }

  protected ThrowStatement self() { return this; }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitThrowStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(original, expressionStatement) && newElement instanceof ExpressionStatement) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }

  public GoloStatement<?> getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement<?> stat) {
    this.expressionStatement = makeParentOf(stat);
  }
}
