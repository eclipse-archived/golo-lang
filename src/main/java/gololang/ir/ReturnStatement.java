/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.Objects;
import java.util.Collections;
import java.util.List;

public final class ReturnStatement extends GoloStatement<ReturnStatement> {

  private GoloStatement<?> expressionStatement;
  private boolean returningVoid;
  private boolean synthetic;

  private ReturnStatement(ExpressionStatement<?> expression) {
    super();
    setExpressionStatement(expression);
    this.returningVoid = false;
    this.synthetic = false;
  }

  /**
   * Creates a {@code return} statement.
   *
   * @param value an object interpreted as an {@link ExpressionStatement}
   * @see ExpressionStatement#of(Object)
   */
  public static ReturnStatement of(Object value) {
    return new ReturnStatement(ExpressionStatement.of(value));
  }

  /**
   * Creates a {@code void} return statement.
   */
  public static ReturnStatement empty() {
    return new ReturnStatement(null).returningVoid();
  }

  protected ReturnStatement self() { return this; }

  public GoloStatement<?> expression() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement<?> stat) {
    if (stat != null) {
      this.expressionStatement = makeParentOf(stat);
    } else {
      this.expressionStatement = null;
    }
  }

  public boolean isReturningVoid() {
    return returningVoid;
  }

  public ReturnStatement returningVoid() {
    this.returningVoid = true;
    return this;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReturnStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    if (expressionStatement != null) {
      return Collections.singletonList(expressionStatement);
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(original, expressionStatement) && newElement instanceof ExpressionStatement) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
