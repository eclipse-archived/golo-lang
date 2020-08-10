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
import java.util.Objects;

/**
 * Represents a {@code foreach} loop on an iterator.
 * <p>For instance:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * foreach x in range(5) {
 *   println(x)
 * }
 * </code></pre>
 */
public final class ForEachLoopStatement extends GoloStatement<ForEachLoopStatement> implements BlockContainer<ForEachLoopStatement>, ReferencesHolder {
  private Block block = Block.empty();
  private ExpressionStatement<?> iterable;
  private final List<LocalReference> valueRefs = new LinkedList<>();
  private ExpressionStatement<?> whenClause;
  private boolean isVarargs = false;

  private ForEachLoopStatement() {
    super();
  }

  /**
   * Complete foreach loop.
   *
   * For meta-generation.
   *
   * @param varargs the parameters of the loop are variable length destructuring
   * @param iterable the iterable we loop on
   * @param when the loop filter
   * @param block the loop block
   * @param vars the loop variables
   */
  public static ForEachLoopStatement create(boolean varargs, Object iterable, Object when, Object block, Object... vars) {
    ForEachLoopStatement loop = new ForEachLoopStatement();
    for (Object v : vars) {
      loop.var(v);
    }
    return loop.varargs(varargs).when(when).in(iterable).block(block);
  }

  public static ForEachLoopStatement create() {
    return new ForEachLoopStatement();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ForEachLoopStatement block(Object block) {
    this.block = makeParentOf(Block.of(block));
    return this;
  }

  protected ForEachLoopStatement self() { return this; }

  /**
   * Defines the iterable on which to loop.
   *
   * <p>This is a builder method.
   *
   * @param iterable an {@link ExpressionStatement} representing an {@code Iterable} expression.
   */
  public ForEachLoopStatement in(Object iterable) {
    this.iterable = ExpressionStatement.of(iterable);
    return this;
  }

  public ForEachLoopStatement varargs(boolean b) {
    this.isVarargs = b;
    return this;
  }

  /**
   * Adds a loop variable.
   *
   * <p>This is a builder method.
   *
   * @param varRef the variable (a {@link LocalReference} create)
   * @see LocalReference#of(Object)
   */
  public ForEachLoopStatement var(Object varRef) {
    this.valueRefs.add(LocalReference.of(varRef).variable());
    return this;
  }

  /**
   * Adds a when clause to the loop.
   *
   * <p>This is a builder method.
   *
   * @param clause an {@link ExpressionStatement} used as a condition.
   */
  public ForEachLoopStatement when(Object clause) {
    if (clause != null) {
      this.whenClause = ExpressionStatement.of(clause);
    } else {
      this.whenClause = null;
    }
    return this;
  }

  public ExpressionStatement<?> getIterable() {
    return iterable;
  }

  public Block getBlock() {
    return block;
  }

  public boolean isDestructuring() {
    return valueRefs.size() > 1;
  }

  public boolean isVarargs() {
    return this.isVarargs;
  }

  public LocalReference getLocalReference() {
    return valueRefs.get(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalReference[] getReferences() {
    return valueRefs.toArray(new LocalReference[valueRefs.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReferencesCount() {
    return valueRefs.size();
  }

  public boolean hasWhenClause() {
    return whenClause != null;
  }

  public ExpressionStatement<?> getWhenClause() {
    return whenClause;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitForEachLoopStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>(valueRefs);
    children.add(iterable);
    if (whenClause != null) {
      children.add(whenClause);
    }
    children.add(block);
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(iterable, original)) {
      in(newElement);
    } else if (Objects.equals(whenClause, original)) {
      when(newElement);
    } else if (Objects.equals(block, original)) {
      block(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
