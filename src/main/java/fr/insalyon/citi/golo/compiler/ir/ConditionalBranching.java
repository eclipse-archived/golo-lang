/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler.ir;

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
