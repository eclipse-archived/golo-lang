/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class ReturnStatement extends GoloStatement {

  private final GoloStatement expressionStatement;
  private boolean returningVoid;

  public ReturnStatement(ExpressionStatement expressionStatement) {
    super();
    this.expressionStatement = expressionStatement;
    this.returningVoid = false;
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }

  public boolean isReturningVoid() {
    return returningVoid;
  }

  public void returningVoid() {
    this.returningVoid = true;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReturnStatement(this);
  }
}
