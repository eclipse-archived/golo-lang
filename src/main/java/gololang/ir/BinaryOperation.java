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

import java.util.Arrays;
import java.util.List;

/**
 * Represents a binary operation.
 *
 * @see OperatorType
 */
public final class BinaryOperation extends ExpressionStatement<BinaryOperation> {

  private final OperatorType type;
  private ExpressionStatement<?> leftExpression;
  private ExpressionStatement<?> rightExpression;

  private BinaryOperation(OperatorType type) {
    super();
    this.type = type;
  }

  /**
   * Create a binary operation.
   *
   * @param type a {@link OperatorType} or a {@code String} representing the operator.
   */
  public static BinaryOperation of(Object type) {
    return new BinaryOperation(OperatorType.of(type));
  }

  /**
   * Full generic binary operation creation in one call.
   *
   * <p>Less readable than the fluent API, but useful when doing meta-generation
   *
   * @param type the type of the operation ({@link BinaryOperation#of(Object)})
   * @param left the left expression.
   * @param right the right expression.
   * @return a configured binary operation.
   */
  public static BinaryOperation create(Object type, Object left, Object right) {
    return of(type).left(left).right(right);
  }

  protected BinaryOperation self() { return this; }

  public OperatorType getType() {
    return type;
  }

  public ExpressionStatement<?> left() {
    return leftExpression;
  }

  /**
   * Define the left expression of this operation.
   *
   * <p>This is a builder method.
   *
   * @param expr the {@link gololang.ir.ExpressionStatement} to use as the left
   * operand
   * @return this operation
   */
  public BinaryOperation left(Object expr) {
    this.leftExpression = makeParentOf(ExpressionStatement.of(expr));
    return this;
  }

  /**
   * Define the right expression of this operation.
   *
   * <p>This is a builder method.
   *
   * @param expr the {@link gololang.ir.ExpressionStatement} to use as the
   * right operand
   * @return this operation
   */
  public BinaryOperation right(Object expr) {
    this.rightExpression = makeParentOf(ExpressionStatement.of(expr));
    if (this.type == OperatorType.ELVIS_METHOD_CALL && this.rightExpression instanceof MethodInvocation) {
      ((MethodInvocation) this.rightExpression).nullSafe(true);
    }
    return this;
  }

  public ExpressionStatement<?> right() {
    return rightExpression;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s", leftExpression, type, rightExpression);
  }

  public boolean isMethodCall() {
    return this.getType() == OperatorType.METHOD_CALL
      || this.getType() == OperatorType.ELVIS_METHOD_CALL
      || this.getType() == OperatorType.ANON_CALL;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBinaryOperation(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Arrays.asList(leftExpression, rightExpression);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (!(newElement instanceof ExpressionStatement)) {
      throw cantConvert("ExpressionStatement", newElement);
    }
    if (leftExpression.equals(original)) {
      left(newElement);
    } else if (rightExpression.equals(original)) {
      right(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
