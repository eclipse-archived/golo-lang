/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public final class Decorator extends  GoloElement {

  private ExpressionStatement expressionStatement;

  private boolean constant = false;

  Decorator(ExpressionStatement expressionStatement) {
    super();
    setExpressionStatement(expressionStatement);
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  public void setExpressionStatement(ExpressionStatement expr) {
    if (!(expr instanceof ReferenceLookup || expr instanceof FunctionInvocation)) {
      throw new IllegalArgumentException("Decorator expression must be a reference of a invocation");
    }
    this.expressionStatement = expr;
    makeParentOf(expr);
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public Decorator constant(boolean constant) {
    setConstant(constant);
    return this;
  }

  public Decorator constant() {
    return constant(true);
  }

  private ExpressionStatement wrapLookup(ReferenceLookup reference, ExpressionStatement expression) {
    return Builders.call(reference.getName())
      .constant(this.isConstant())
      .withArgs(expression);
  }

  private ExpressionStatement wrapInvocation(FunctionInvocation invocation, ExpressionStatement expression) {
    return invocation.followedBy(
        Builders.functionInvocation()
        .constant(this.isConstant())
        .withArgs(expression));
  }

  public ExpressionStatement wrapExpression(ExpressionStatement expression) {
    if (expressionStatement instanceof ReferenceLookup) {
      return wrapLookup((ReferenceLookup) expressionStatement, expression);
    }
    return wrapInvocation((FunctionInvocation) expressionStatement, expression);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitDecorator(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expressionStatement.equals(original) && newElement instanceof ExpressionStatement) {
      setExpressionStatement((ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
