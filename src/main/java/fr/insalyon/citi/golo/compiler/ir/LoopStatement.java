/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class LoopStatement extends GoloStatement {

  private final AssignmentStatement initStatement;
  private final ExpressionStatement conditionStatement;
  private final GoloStatement postStatement;
  private final Block block;
  private boolean hasWhen = false;

  public LoopStatement(AssignmentStatement initStatement, ExpressionStatement conditionStatement, Block block, GoloStatement postStatement) {
    super();
    this.initStatement = initStatement;
    this.conditionStatement = conditionStatement;
    this.postStatement = postStatement;
    this.block = block;
  }

  public void setHasWhen(boolean hw) {
    this.hasWhen = hw;
  }

  public boolean hasInitStatement() {
    return initStatement != null;
  }

  public AssignmentStatement getInitStatement() {
    return initStatement;
  }

  public ExpressionStatement getConditionStatement() {
    return conditionStatement;
  }

  public Block getBlock() {
    return block;
  }

  public Block getMainBlock() {
    if (hasWhen) {
      for (GoloStatement stm : block.getStatements()) {
        if (stm instanceof AssignmentStatement) continue;
        return ((ConditionalBranching) stm).getTrueBlock();
      }
    }
    return block;
  }

  public GoloStatement getPostStatement() {
    return postStatement;
  }

  public boolean hasPostStatement() {
    return postStatement != null;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }
}
