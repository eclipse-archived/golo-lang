/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public abstract class GoloAssignment extends GoloStatement implements ReferencesHolder {

  private boolean declaring = false;
  private ExpressionStatement expressionStatement;

  GoloAssignment() { super(); }

  public boolean isDeclaring() {
    return declaring;
  }

  public GoloAssignment declaring(boolean isDeclaring) {
    this.declaring = isDeclaring;
    return this;
  }

  public GoloAssignment declaring() {
    return declaring(true);
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  /**
   * Defines the value to be assigned.
   */
  public GoloAssignment as(Object expr) {
    expressionStatement = ExpressionStatement.of(expr);
    makeParentOf(expressionStatement);
    return this;
  }

  /**
   * Defines the reference to assign to.
   */
  public abstract GoloAssignment to(Object... refs);


  /**
   * {@inheritDoc}
   */
  @Override
  public LocalReference[] getDeclaringReferences() {
    if (declaring) {
      return getReferences();
    }
    return new LocalReference[0];
  }

  /**
   * @inheritDoc
   */
  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (original.equals(getExpressionStatement()) && newElement instanceof ExpressionStatement) {
      as(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }


}
