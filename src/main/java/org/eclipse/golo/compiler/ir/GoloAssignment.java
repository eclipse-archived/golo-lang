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

public abstract class GoloAssignment<T extends GoloAssignment<T>> extends GoloStatement<T> implements ReferencesHolder {

  private boolean declaring = false;
  private ExpressionStatement<?> expressionStatement;

  GoloAssignment() { super(); }

  public final boolean isDeclaring() {
    return declaring;
  }

  public final T declaring(boolean isDeclaring) {
    this.declaring = isDeclaring;
    return self();
  }

  public final T declaring() {
    return this.declaring(true);
  }

  public final ExpressionStatement<?> getExpressionStatement() {
    return this.expressionStatement;
  }

  /**
   * Defines the value to be assigned.
   */
  public final T as(Object expr) {
    this.expressionStatement = makeParentOf(ExpressionStatement.of(expr));
    return self();
  }

  /**
   * Defines the reference to assign to.
   */
  public abstract T to(Object... refs);


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
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
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
