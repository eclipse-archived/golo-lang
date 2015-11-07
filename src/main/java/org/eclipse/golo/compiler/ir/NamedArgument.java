/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public final class NamedArgument extends ExpressionStatement {

  private String name;
  private ExpressionStatement expression;

  NamedArgument(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ExpressionStatement getExpression() {
    return expression;
  }

  public NamedArgument value(Object value) {
    expression = (ExpressionStatement) value;
    makeParentOf(expression);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNamedArgument(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expression.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expression != original) {
      throw doesNotContain(original);
    }
    value((ExpressionStatement) newElement);
  }
}
