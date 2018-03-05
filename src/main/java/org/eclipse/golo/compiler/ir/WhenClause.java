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

public final class WhenClause<T extends GoloElement<?>> extends GoloElement<WhenClause<T>> {
  private ExpressionStatement<?> condition;
  private T action;

  WhenClause(ExpressionStatement<?> condition, T action) {
    this.condition = makeParentOf(condition);
    setAction(action);
  }

  protected WhenClause<T> self() { return this; }

  public ExpressionStatement<?> condition() { return this.condition; }

  public T action() { return this.action; }

  public void setAction(T a) {
    this.action = makeParentOf(a);
  }

  @Override
  public String toString() {
    return String.format("when %s then %s", condition, action);
  }

  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (this.condition.equals(original)) {
      if (!(newElement instanceof ExpressionStatement)) {
        throw cantConvert("ExpressionStatement", newElement);
      }
      this.condition = makeParentOf(ExpressionStatement.of(newElement));
    } else if (this.action.equals(original)) {
      @SuppressWarnings("unchecked")
      T element = (T) newElement;
      setAction(element);
    } else {
      throw doesNotContain(original);
    }
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitWhenClause(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    this.condition.accept(visitor);
    this.action.accept(visitor);
  }
}


