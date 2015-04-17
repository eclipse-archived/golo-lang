/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class NamedArgument extends ExpressionStatement {

  private String name;
  private ExpressionStatement expression;

  public NamedArgument(String name, ExpressionStatement expression) {
    this.name = name;
    this.expression = expression;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ExpressionStatement getExpression() {
    return expression;
  }

  public void setExpression(ExpressionStatement expression) {
    this.expression = expression;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {

  }
}
