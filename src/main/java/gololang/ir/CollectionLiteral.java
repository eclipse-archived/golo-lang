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

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import gololang.ir.CollectionLiteral.Type;

/**
 * Represents a collection literal.
 *
 * <p>For instance code such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * list[1, 2, 3]
 * </code></pre>
 * @see Type
 */
public final class CollectionLiteral extends ExpressionStatement<CollectionLiteral> {

  public enum Type {
    array, list, set, map, tuple, vector, range
  }

  private final Type type;
  private final List<ExpressionStatement<?>> expressions = new LinkedList<>();

  private CollectionLiteral(Type type) {
    super();
    this.type = type;
  }

  /**
   * Creates a collection literal of the given type.
   *
   * <p>For instance:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * collection(CollectionLiteral.Type.array, constant(1), constant(2), constant(3))
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * array[1, 2, 3]
   * </code></pre>
   *
   * @param type the type
   * @param values expressions to add to the collection
   */
  public static CollectionLiteral create(Object type, Object... values) {
    CollectionLiteral col = new CollectionLiteral(
        type instanceof CollectionLiteral.Type
          ? (CollectionLiteral.Type) type
          : CollectionLiteral.Type.valueOf(type.toString()));
    for (Object v : values) {
      col.add(v);
    }
    return col;
  }


  protected CollectionLiteral self() { return this; }

  /**
   * Adds an expression to the collection.
   *
   * @param expression the expression to add.
   * @return the collection literal.
   */
  public CollectionLiteral add(Object expression) {
    this.expressions.add(makeParentOf(ExpressionStatement.of(expression)));
    return this;
  }

  public Type getType() {
    return type;
  }

  public List<ExpressionStatement<?>> getExpressions() {
    return Collections.unmodifiableList(expressions);
  }

  @Override
  public String toString() {
    return this.type.toString() + this.expressions.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCollectionLiteral(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.unmodifiableList(expressions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (expressions.contains(original) && newElement instanceof ExpressionStatement) {
      expressions.set(expressions.indexOf(original), ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
