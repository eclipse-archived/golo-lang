/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public final class WhenClause<T extends GoloElement> extends GoloElement {
  private ExpressionStatement condition;
  private T action;

  WhenClause(ExpressionStatement condition, T action) {
    this.condition = condition;
    makeParentOf(condition);
    setAction(action);
  }

  public ExpressionStatement condition() { return this.condition; }

  public T action() { return this.action; }

  public void setAction(T a) {
    this.action = a;
    makeParentOf(a);
  }

  @Override
  public String toString() {
    return String.format("when %s then %s", condition, action);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (condition.equals(original)) {
      if (!(newElement instanceof ExpressionStatement)) {
        throw cantConvert("ExpressionStatement", newElement);
      }
      this.condition = (ExpressionStatement) newElement;
      makeParentOf(this.condition);
    } else if (action.equals(original)) {
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


