/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.runtime.OperatorType;

public class UnaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private final ExpressionStatement expressionStatement;

  public UnaryOperation(OperatorType type, ExpressionStatement expressionStatement) {
    super();
    this.type = type;
    this.expressionStatement = expressionStatement;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  public OperatorType getType() {
    return type;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnaryOperation(this);
  }
}
