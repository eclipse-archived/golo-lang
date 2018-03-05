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

public final class UnaryOperation extends ExpressionStatement<UnaryOperation> {

  private final OperatorType type;
  private ExpressionStatement<?> expressionStatement;

  UnaryOperation(OperatorType type, ExpressionStatement<?> expressionStatement) {
    super();
    this.type = type;
    setExpressionStatement(expressionStatement);
  }

  protected UnaryOperation self() { return this; }

  public ExpressionStatement<?> getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(ExpressionStatement<?> statement) {
    this.expressionStatement = makeParentOf(statement);
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
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (expressionStatement.equals(original)) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
