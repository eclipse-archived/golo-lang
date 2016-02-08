/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.runtime.OperatorType;

public class UnaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private ExpressionStatement expressionStatement;

  UnaryOperation(OperatorType type, ExpressionStatement expressionStatement) {
    super();
    this.type = type;
    setExpressionStatement(expressionStatement);
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(ExpressionStatement statement) {
    this.expressionStatement = statement;
    makeParentOf(statement);
  }

  public OperatorType getType() {
    return type;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnaryOperation(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expressionStatement.equals(original)) {
      setExpressionStatement((ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
