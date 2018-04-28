/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.Collections;
import java.util.List;

public final class UnaryOperation extends ExpressionStatement<UnaryOperation> {

  private final OperatorType type;
  private ExpressionStatement<?> expressionStatement;

  UnaryOperation(OperatorType type, ExpressionStatement<?> expressionStatement) {
    super();
    this.type = type;
    setExpressionStatement(expressionStatement);
  }

  /**
   * Creates a generic unary operation.
   */
  public static UnaryOperation create(Object type, Object expression) {
    return new UnaryOperation(OperatorType.of(type), ExpressionStatement.of(expression));
  }

  protected UnaryOperation self() { return this; }

  public ExpressionStatement<?> expression() {
    return expressionStatement;
  }

  private void setExpressionStatement(ExpressionStatement<?> statement) {
    this.expressionStatement = makeParentOf(statement);
  }

  public OperatorType getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnaryOperation(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.singletonList(expressionStatement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (expressionStatement.equals(original)) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
