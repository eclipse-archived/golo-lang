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

import static gololang.Messages.message;

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

  /**
   * {@inheritDoc}
   *
   * <p>Always throws an exception since {@link NamedArgument} can't have a local declaration.
   */
  @Override
  public ExpressionStatement with(Object a) {
    throw new UnsupportedOperationException(message("invalid_local_definition", this.getClass().getName()));
  }

  @Override
  public boolean hasLocalDeclarations() {
    return false;
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
