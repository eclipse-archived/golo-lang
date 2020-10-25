/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.Arrays;
import java.util.List;
import gololang.Tuple;
import org.eclipse.golo.runtime.InvalidDestructuringException;

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

  private void setAction(T a) {
    this.action = makeParentOf(a);
  }

  public WhenClause<T> then(T action) {
    setAction(action);
    return this;
  }

  @Override
  public String toString() {
    return String.format("when %s then %s", condition, action);
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitWhenClause(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Arrays.asList(condition, action);
  }

  /**
   * Destructuring helper.
   * @deprecated This method should not be called directly and is no more used by new style destructuring.
   */
  @Deprecated
  public Tuple destruct() {
    return new Tuple(condition, action);
  }

  /**
   * New style destructuring helper.
   *
   * <p>The destructuring must be to exactly two values. No remainer syntax is allowed.
   * <p>The destructured values are the condition and the action.
   *
   * @param number number of variable that will be affected.
   * @param substruct whether the destructuring is complete or should contains a sub structure.
   * @param toSkip a boolean array indicating the elements to skip.
   * @return an array containing the values to assign.
   */
  public Object[] __$$_destruct(int number, boolean substruct, Object[] toSkip) {
    if (number == 2 && !substruct) {
      return new Object[]{condition, action};
    }
    throw new InvalidDestructuringException("A WhenClause must destructure to exactly two values");
  }
}


