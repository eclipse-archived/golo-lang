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

/**
 * Represents a conditional branching.
 *
 * <p>Typically an {@code if else} node.
 */
public final class ConditionalBranching extends GoloStatement<ConditionalBranching> {

  private ExpressionStatement<?> condition;
  private Block trueBlock;
  private ConditionalBranching elseConditionalBranching;
  private Block falseBlock;

  private ConditionalBranching() {
    super();
  }

  /**
   * Creates an empty conditional branch.
   */
  public static ConditionalBranching branch() {
    return new ConditionalBranching();
  }

  /**
   * Full branch construction in one call.
   *
   * <p>Less readable than the fluent API, but useful when doing meta-generation
   *
   * @param condition the test condition
   * @param trueBlock the block to execute when the condition is true
   * @param falseBlock the block to execute when the condition is false
   * @param elseBranch a nested conditional branch
   */
  public static ConditionalBranching create(Object condition,
                                            Object trueBlock,
                                            Object falseBlock,
                                            Object elseBranch) {
    return new ConditionalBranching().condition(condition)
      .whenTrue(trueBlock)
      .whenFalse(falseBlock)
      .elseBranch(elseBranch);
  }

  protected ConditionalBranching self() { return this; }

  /**
   * Defines the condition of the branching.
   *
   * <p>This is a builder method.
   *
   * @param cond the expression defining the condition. If {@code null}, the condition is set to {@code false}.
   * @return the branching.
   */
  public ConditionalBranching condition(Object cond) {
    if (cond == null) {
      this.condition = ConstantStatement.of(false);
    } else {
      this.condition = ExpressionStatement.of(cond);
    }
    makeParentOf(this.condition);
    return this;
  }

  /**
   * Defines the block executed when the condition evaluates to {@code true}.
   *
   * <p>This is a builder method.
   */
  public ConditionalBranching whenTrue(Object block) {
    this.trueBlock = makeParentOf(Block.of(block));
    return this;
  }

  /**
   * Defines the block executed when the condition evaluates to {@code false}.
   *
   * <p>This is a builder method.
   *
   * @see #otherwise(Object)
   */
  public ConditionalBranching whenFalse(Object block) {
    if (block == null) {
      this.falseBlock = null;
    } else {
      this.falseBlock = makeParentOf(Block.of(block));
    }
    return this;
  }

  /**
   * Defines a nested conditional branch.
   *
   * <p>This is a builder method.
   *
   * <p>This represents an {@code else if} branch.
   *
   * @see #otherwise(Object)
   */
  public ConditionalBranching elseBranch(Object elseBranch) {
    this.elseConditionalBranching = makeParentOf((ConditionalBranching) elseBranch);
    return this;
  }

  /**
   * Defines a block to execute when {@code false} or a nested branch according to the given element.
   *
   * <p>This is a builder method; it's the preferred way to define an alternative to the true block.
   *
   * @param alternative a {@link Block} to execute when {@code false} or a {@code ConditionalBranching} defining a
   * nested branch.
   */
  public ConditionalBranching otherwise(Object alternative) {
    if (alternative instanceof ConditionalBranching) {
      return elseBranch(alternative);
    }
    return whenFalse(alternative);
  }

  public ExpressionStatement<?> getCondition() {
    return condition;
  }

  public Block getTrueBlock() {
    return trueBlock;
  }

  public Block getFalseBlock() {
    return falseBlock;
  }

  public boolean hasFalseBlock() {
    return falseBlock != null;
  }

  public ConditionalBranching getElseConditionalBranching() {
    return elseConditionalBranching;
  }

  public boolean hasElseConditionalBranching() {
    return elseConditionalBranching != null;
  }

  public boolean returnsFromBothBranches() {
    if (hasFalseBlock()) {
      return trueBlock.hasReturn() && falseBlock.hasReturn();
    } else if (hasElseConditionalBranching()) {
      return trueBlock.hasReturn() && elseConditionalBranching.returnsFromBothBranches();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return String.format("if %s %s%s", condition, trueBlock,
        hasFalseBlock() ? " else " + falseBlock.toString()
        : hasElseConditionalBranching() ? " else " + elseConditionalBranching.toString()
        : "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConditionalBranching(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>();
    children.add(condition);
    children.add(trueBlock);
    if (falseBlock != null) {
      children.add(falseBlock);
    }
    if (elseConditionalBranching != null) {
      children.add(elseConditionalBranching);
    }
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (condition == original) {
      condition(newElement);
    } else if (trueBlock == original) {
      whenTrue(newElement);
    } else if (elseConditionalBranching == original && newElement instanceof ConditionalBranching) {
      elseBranch(newElement);
    } else if (elseConditionalBranching == original) {
      whenFalse(newElement instanceof Noop ? null : newElement);
      elseBranch(null);
    } else if (falseBlock == original && newElement instanceof ConditionalBranching) {
      elseBranch(newElement);
      whenFalse(null);
    } else if (falseBlock == original) {
      whenFalse(newElement instanceof Noop ? null : newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

}
