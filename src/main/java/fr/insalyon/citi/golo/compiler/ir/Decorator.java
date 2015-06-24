/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class Decorator extends  GoloElement {

  private final ExpressionStatement expressionStatement;

  private boolean constant = false;

  public Decorator(ExpressionStatement expressionStatement) {
    this.expressionStatement = expressionStatement;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitDecorator(this);
  }
}
