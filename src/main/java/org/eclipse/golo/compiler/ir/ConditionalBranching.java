/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class ConditionalBranching extends GoloStatement implements Scope {

  private ExpressionStatement condition;
  private Block trueBlock;
  private ConditionalBranching elseConditionalBranching;
  private Block falseBlock;

  ConditionalBranching() {
    super();
  }

  public ConditionalBranching condition(Object cond) {
    if (cond == null) {
      setCondition(Builders.constant(false));
    } else {
      setCondition((ExpressionStatement) cond);
    }
    return this;
  }

  public ConditionalBranching whenTrue(Object block) {
    setTrueBlock(Builders.toBlock(block));
    return this;
  }

  public ConditionalBranching whenFalse(Object block) {
    setFalseBlock(block == null ? null : Builders.toBlock(block));
    return this;
  }

  public ConditionalBranching elseBranch(Object elseBranch) {
    this.elseConditionalBranching = (ConditionalBranching) elseBranch;
    makeParentOf(elseConditionalBranching);
    return this;
  }

  public ConditionalBranching otherwise(Object alternative) {
    if (alternative instanceof ConditionalBranching) {
      return elseBranch((ConditionalBranching) alternative);
    }
    return whenFalse(alternative);
  }

  @Override
  public ConditionalBranching ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public void relink(ReferenceTable table) {
    trueBlock.relink(table);
    if (falseBlock != null) {
      falseBlock.relink(table);
    }
    if (elseConditionalBranching != null) {
      elseConditionalBranching.relink(table);
    }
  }

  @Override
  public void relinkTopLevel(ReferenceTable table) {
    trueBlock.relinkTopLevel(table);
    if (falseBlock != null) {
      falseBlock.relinkTopLevel(table);
    }
    if (elseConditionalBranching != null) {
      elseConditionalBranching.relinkTopLevel(table);
    }
  }

  public ExpressionStatement getCondition() {
    return condition;
  }

  public void setCondition(ExpressionStatement condition) {
    this.condition = condition;
    makeParentOf(condition);
  }

  public Block getTrueBlock() {
    return trueBlock;
  }

  public void setTrueBlock(Block block) {
    this.trueBlock = block;
    makeParentOf(block);
  }

  public Block getFalseBlock() {
    return falseBlock;
  }

  public void setFalseBlock(Block block) {
    this.falseBlock = block;
    if (block != null) {
      makeParentOf(block);
    }
  }

  public boolean hasFalseBlock() {
    return falseBlock != null;
  }

  public ConditionalBranching getElseConditionalBranching() {
    return elseConditionalBranching;
  }

  public void setElseConditionalBranching(ConditionalBranching elseBranch) {
    this.elseConditionalBranching = elseBranch;
    makeParentOf(elseBranch);
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

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConditionalBranching(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    condition.accept(visitor);
    trueBlock.accept(visitor);
    if (falseBlock != null) {
      falseBlock.accept(visitor);
    }
    if (elseConditionalBranching != null) {
      elseConditionalBranching.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (condition == original && newElement instanceof ExpressionStatement) {
      condition(newElement);
    } else if (elseConditionalBranching == original && newElement instanceof ConditionalBranching) {
      elseBranch(newElement);
    } else if (trueBlock == original) {
      whenTrue(newElement);
    } else if (falseBlock == original) {
      whenFalse(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

}
