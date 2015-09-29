/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public final class ConditionalBranching extends GoloStatement {

  private final ExpressionStatement condition;
  private final Block trueBlock;
  private final ConditionalBranching elseConditionalBranching;
  private final Block falseBlock;

  public ConditionalBranching(ExpressionStatement condition, Block trueBlock, Block falseBlock) {
    super();
    this.condition = condition;
    this.trueBlock = trueBlock;
    this.falseBlock = falseBlock;
    this.elseConditionalBranching = null;
  }

  public ConditionalBranching(ExpressionStatement condition, Block trueBlock, ConditionalBranching elseConditionalBranching) {
    super();
    this.condition = condition;
    this.trueBlock = trueBlock;
    this.elseConditionalBranching = elseConditionalBranching;
    this.falseBlock = null;
  }

  public ExpressionStatement getCondition() {
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
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConditionalBranching(this);
  }
}
