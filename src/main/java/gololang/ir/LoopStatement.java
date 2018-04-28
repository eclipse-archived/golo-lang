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

import java.util.Objects;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic looping structure.
 *
 * <p>This is used to represent both {@code for} and {@code while} loops, as well as the desugared {@code foreach} loop.
 */
public final class LoopStatement extends GoloStatement<LoopStatement> implements BlockContainer<LoopStatement>, ReferencesHolder {

  private AssignmentStatement initStatement = null;
  private ExpressionStatement<?> conditionStatement = null;
  private GoloStatement<?> postStatement = null;
  private Block block = null;

  private LoopStatement() {
    super();
  }

  /**
   * Creates a generic loop.
   */
  public static LoopStatement loop() {
    return new LoopStatement().condition(null).block(null);
  }

  /**
   * Complete loop creation.
   *
   * For meta-generation.
   */
  public static LoopStatement create(Object init, Object condition, Object post, Object block) {
    return loop().init(init).condition(condition).post(post).block(Block.of(block));
  }


  protected LoopStatement self() { return this; }

  /**
   * Defines the initialization of the loop variable.
   *
   * <p>For instance, the <code class="lang-golo">var i = 0</code> statement.
   * This can be {@code null} in the case of {@code while} loops.
   *
   * <p>This is a builder method.
   *
   * @param assignment a {@link AssignmentStatement} defining the loop variable.
   */
  public LoopStatement init(Object assignment) {
    if (assignment instanceof AssignmentStatement) {
      this.initStatement = makeParentOf((AssignmentStatement) assignment);
      return this;
    }
    if (assignment == null) {
      this.initStatement = null;
      return this;
    }
    throw cantConvert("assignment", assignment);
  }

  /**
   * Defines the loop condition.
   *
   * <p>For instance, the <code class="lang-golo">i < 10</code> expression.
   *
   * <p>This is a builder method.
   *
   * @param expression a {@link ExpressionStatement} evaluating to a boolean.
   */
  public LoopStatement condition(Object expression) {
    if (expression == null) {
      this.conditionStatement = ConstantStatement.of(false);
    } else if (expression instanceof ExpressionStatement) {
      this.conditionStatement = ExpressionStatement.of(expression);
    } else {
      throw cantConvert("expression", expression);
    }
    makeParentOf(this.conditionStatement);
    return this;
  }

  /**
   * Define the post-loop statement.
   *
   * <p>For instance, the <code class="lang-golo">i = i + 1</code> statement.
   * This can be {@code null} in the case of {@code while} loops.
   *
   * <p>This is a builder method.
   *
   * @param statement a {@link GoloStatement} changing the loop variable.
   */
  public LoopStatement post(Object statement) {
    if (statement instanceof GoloStatement) {
      this.postStatement = makeParentOf((GoloStatement) statement);
      return this;
    }
    if (statement == null) {
      this.postStatement = null;
      return this;
    }
    throw cantConvert("statement", statement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LoopStatement block(Object innerBlock) {
    this.block = makeParentOf(Block.of(innerBlock));
    return this;
  }

  public boolean hasInitStatement() {
    return initStatement != null;
  }

  public AssignmentStatement init() {
    return initStatement;
  }

  public ExpressionStatement<?> condition() {
    return conditionStatement;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Block getBlock() {
    return block;
  }

  public GoloStatement<?> post() {
    return postStatement;
  }

  public boolean hasPostStatement() {
    return postStatement != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalReference[] getReferences() {
    if (hasInitStatement()) {
      return new LocalReference[]{init().getLocalReference()};
    }
    return new LocalReference[0];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReferencesCount() {
    return hasInitStatement() ? 1 : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>();
    if (initStatement != null) {
      children.add(initStatement);
    }
    if (conditionStatement != null) {
      children.add(conditionStatement);
    }
    if (postStatement != null) {
      children.add(postStatement);
    }
    if (block != null) {
      children.add(block);
    }
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(initStatement, original)) {
      init(newElement);
    } else if (Objects.equals(conditionStatement, original)) {
      condition(newElement);
    } else if (Objects.equals(postStatement, original)) {
      post(newElement);
    } else if (Objects.equals(block, original)) {
      block(Block.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
