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
import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class BinaryOperation extends ExpressionStatement {
  private final OperatorType type;
  private ExpressionStatement leftExpression;
  private ExpressionStatement rightExpression;

  BinaryOperation(OperatorType type) {
    super();
    this.type = type;
  }

  public static BinaryOperation of(Object type) {
    if (type instanceof OperatorType) {
      return new BinaryOperation((OperatorType) type);
    }
    if (type instanceof String) {
      return new BinaryOperation(OperatorType.fromString((String) type));
    }
    throw cantConvert("BinaryOperation", type);
  }

  @Override
  public BinaryOperation ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public OperatorType getType() {
    return type;
  }

  public ExpressionStatement getLeftExpression() {
    return leftExpression;
  }

  public BinaryOperation left(Object expr) {
    leftExpression = (ExpressionStatement) expr;
    makeParentOf(leftExpression);
    return this;
  }

  public BinaryOperation right(Object expr) {
    rightExpression = (ExpressionStatement) expr;
    makeParentOf(rightExpression);
    return this;
  }

  public ExpressionStatement getRightExpression() {
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

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBinaryOperation(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    leftExpression.accept(visitor);
    rightExpression.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
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
