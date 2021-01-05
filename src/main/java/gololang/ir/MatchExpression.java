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
 * A {@code match} node.
 * <p> This node describe tree such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 *  match {
 *    when condition_1 then expression_1
 *    when condition_2 then expression_2
 *    otherwise default_expression
 *  }
 * </code></pre>
 */
public final class MatchExpression extends ExpressionStatement<MatchExpression> implements Alternatives<ExpressionStatement<?>> {

  private ExpressionStatement<?> otherwise;
  private final LinkedList<WhenClause<ExpressionStatement<?>>> clauses = new LinkedList<>();

  MatchExpression() {
    super();
  }

  /**
   * Creates an empty match expression.
   */
  public static MatchExpression match() {
    return new MatchExpression();
  }

  protected MatchExpression self() { return this; }

  /**
   * {@inheritDoc}
   */
  @Override
  public MatchExpression when(Object cond) {
    if (cond instanceof WhenClause) {
      @SuppressWarnings("unchecked")
      WhenClause<ExpressionStatement<?>> clause = (WhenClause<ExpressionStatement<?>>) cond;
      this.clauses.add(makeParentOf(clause));
    } else {
      this.clauses.add(makeParentOf(new WhenClause<ExpressionStatement<?>>(ExpressionStatement.of(cond), null)));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @param action a {@link gololang.ir.ExpressionStatement} containing the expression to return.
   */
  @Override
  public MatchExpression then(Object action) {
    this.clauses.getLast().then(ExpressionStatement.of(action));
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @param action a {@link gololang.ir.ExpressionStatement} containing the expression to return.
   */
  @Override
  public MatchExpression otherwise(Object action) {
    this.otherwise = makeParentOf(ExpressionStatement.of(action));
    return this;
  }

  public List<WhenClause<ExpressionStatement<?>>> getClauses() {
    return unmodifiableList(this.clauses);
  }

  public ExpressionStatement<?> getOtherwise() {
    return this.otherwise;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMatchExpression(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>(clauses);
    children.add(otherwise);
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (!(newElement instanceof ExpressionStatement || newElement instanceof WhenClause)) {
      throw cantConvert("ExpressionStatement or WhenClause", newElement);
    }
    if (otherwise.equals(original)) {
      otherwise(newElement);
      return;
    }
    if (clauses.contains(original)) {
      @SuppressWarnings("unchecked")
      WhenClause<ExpressionStatement<?>> when = (WhenClause<ExpressionStatement<?>>) newElement;
      clauses.set(clauses.indexOf(original), makeParentOf(when));
      return;
    }
    throw doesNotContain(original);
  }
}
