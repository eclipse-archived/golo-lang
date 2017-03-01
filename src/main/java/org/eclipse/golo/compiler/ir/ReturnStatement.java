/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Objects;

public final class ReturnStatement extends GoloStatement implements Scope {

  private GoloStatement expressionStatement;
  private boolean returningVoid;
  private boolean synthetic;

  ReturnStatement(ExpressionStatement expression) {
    super();
    setExpressionStatement(expression);
    this.returningVoid = false;
    this.synthetic = false;
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement stat) {
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
  public void relink(ReferenceTable table) {
    if (expressionStatement instanceof Scope) {
      ((Scope) expressionStatement).relink(table);
    }
  }

  @Override
  public void relinkTopLevel(ReferenceTable table) {
    if (expressionStatement instanceof Scope) {
      ((Scope) expressionStatement).relinkTopLevel(table);
    }
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
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (Objects.equals(original, expressionStatement) && newElement instanceof ExpressionStatement) {
      setExpressionStatement((ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
