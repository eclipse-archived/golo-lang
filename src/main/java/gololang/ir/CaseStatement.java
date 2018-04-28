/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * A {@code case} node.
 * <p> This node describe tree such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 *  case {
 *    when condition_1 {
 *      action_1
 *    }
 *    when condition_2 {
 *      action_2
 *    }
 *    otherwise {
 *      default_action
 *    }
 *  }
 * </code></pre>
 */
public final class CaseStatement extends GoloStatement<CaseStatement> implements Alternatives<Block> {

  private Block otherwise;
  private final LinkedList<WhenClause<Block>> clauses = new LinkedList<>();

  private CaseStatement() {
    super();
  }

  /**
   * Creates an empty case switch.
   */
  public static CaseStatement cases() {
    return new CaseStatement();
  }

  protected CaseStatement self() { return this; }

  /**
   * {@inheritDoc}
   */
  @Override
  public CaseStatement when(Object cond) {
    if (cond instanceof WhenClause) {
      @SuppressWarnings("unchecked")
      WhenClause<Block> clause = (WhenClause<Block>) cond;
      this.clauses.add(makeParentOf(clause));
    } else {
      this.clauses.add(makeParentOf(new WhenClause<Block>(ExpressionStatement.of(cond), null)));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @param action a {@link gololang.ir.Block} containing the statements to execute.
   */
  @Override
  public CaseStatement then(Object action) {
    this.clauses.getLast().then(Block.of(action));
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @param action a {@link gololang.ir.Block} containing the statements to execute.
   */
  @Override
  public CaseStatement otherwise(Object action) {
    this.otherwise = makeParentOf(Block.of(action));
    return this;
  }

  public List<WhenClause<Block>> getClauses() {
    return unmodifiableList(this.clauses);
  }

  public Block getOtherwise() {
    return this.otherwise;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCaseStatement(this);
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
  public void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (!(newElement instanceof Block || newElement instanceof WhenClause)) {
      throw cantConvert("Block or WhenClause", newElement);
    }
    if (otherwise.equals(original)) {
      otherwise(newElement);
      return;
    }
    if (clauses.contains(original)) {
      @SuppressWarnings("unchecked")
      WhenClause<Block> when = (WhenClause<Block>) newElement;
      clauses.set(clauses.indexOf(original), makeParentOf(when));
      return;
    }
    throw doesNotContain(original);
  }
}
