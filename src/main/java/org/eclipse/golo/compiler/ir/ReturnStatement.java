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

public final class ReturnStatement extends GoloStatement<ReturnStatement> {

  private GoloStatement<?> expressionStatement;
  private boolean returningVoid;
  private boolean synthetic;

  ReturnStatement(ExpressionStatement<?> expression) {
    super();
    setExpressionStatement(expression);
    this.returningVoid = false;
    this.synthetic = false;
  }

  protected ReturnStatement self() { return this; }

  public GoloStatement<?> getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement<?> stat) {
    this.expressionStatement = stat;
    makeParentOf(stat);
  }

  public boolean isReturningVoid() {
    return returningVoid;
  }

  public void returningVoid() {
    this.returningVoid = true;
  }

  public ReturnStatement synthetic() {
    this.synthetic = true;
    return this;
  }

  public boolean isSynthetic() {
    return this.synthetic;
  }

  @Override
  public String toString() {
    return "return " + (
        returningVoid || expressionStatement == null
        ? ""
        : expressionStatement.toString());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReturnStatement(this);
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
}
