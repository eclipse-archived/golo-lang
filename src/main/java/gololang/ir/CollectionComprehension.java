/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
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

import static java.util.Collections.unmodifiableList;

/**
 * Represents a collection comprehension expression.
 *
 * <p>Such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * list[2*x + y foreach x in someIterable for (var y=0, y &lt; 5, y = y + 1)]
 * </code></pre>
 *
 * @see CollectionLiteral.Type
 */
public final class CollectionComprehension extends ExpressionStatement<CollectionComprehension> {

  private final CollectionLiteral.Type type;
  private ExpressionStatement<?> expression;
  private final List<GoloStatement<?>> loopBlocks = new LinkedList<>();

  private CollectionComprehension(CollectionLiteral.Type type) {
    super();
    this.type = type;
  }

  /**
   * Creates a collection comprehension of the given type.
   */
  public static CollectionComprehension of(Object type) {
    return new CollectionComprehension(
        (type instanceof CollectionLiteral.Type)
        ? (CollectionLiteral.Type) type
        : CollectionLiteral.Type.valueOf(type.toString()));
  }

  /**
   * Complete collection comprehension creation.
   *
   * For meta-generation.
   */
  public static CollectionComprehension create(Object type, Object expression, Object... loops) {
    CollectionComprehension c = of(type).expression(expression);
    for (Object l : loops) {
      c.loop(l);
    }
    return c;
  }

  protected CollectionComprehension self() { return this; }

  /**
   * Defines the expression of the comprehension.
   *
   * <code>2*x + y</code> in the previous example.
   */
  public CollectionComprehension expression(Object expression) {
    this.expression = makeParentOf(ExpressionStatement.of(expression));
    return this;
  }

  /**
   * Adds a loop instruction to this comprehension.
   *
   * <p>The object to add is a loop-like expression, <code>foreach x in someIterable</code> and
   * <code>for (var y=0, y &lt; 5, y = y + 1)</code> in the previous example.
   * @param loop a {@link ForEachLoopStatement} or a {@link LoopStatement}.
   */
  public CollectionComprehension loop(Object loop) {
    if (!(loop instanceof ForEachLoopStatement || loop instanceof LoopStatement)) {
      throw new IllegalArgumentException("Loop expected, got a " + loop.getClass().getName());
    }
    this.loopBlocks.add(makeParentOf(GoloStatement.of(loop)));
    return this;
  }

  public ExpressionStatement<?> expression() {
    return this.expression;
  }

  public List<GoloStatement<?>> loops() {
    return unmodifiableList(loopBlocks);
  }

  public CollectionLiteral.Type getType() {
    return this.type;
  }

  public CollectionLiteral.Type getMutableType() {
    return (CollectionLiteral.Type.tuple.equals(type)
       || CollectionLiteral.Type.array.equals(type))
      ? CollectionLiteral.Type.list
      : type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCollectionComprehension(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>();
    children.add(expression);
    children.addAll(loopBlocks);
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (expression == original && newElement instanceof ExpressionStatement) {
      expression(newElement);
    } else if (newElement instanceof Block && loopBlocks.contains(original)) {
      loopBlocks.set(loopBlocks.indexOf(original), Block.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
