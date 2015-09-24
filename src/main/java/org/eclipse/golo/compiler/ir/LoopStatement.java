/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;
import java.util.Objects;

import static org.eclipse.golo.compiler.ir.Builders.*;

public final class LoopStatement extends GoloStatement implements Scope, BlockContainer {

  private AssignmentStatement initStatement = null;
  private ExpressionStatement conditionStatement = constant(false);
  private GoloStatement postStatement = null;
  private Block block = Block.emptyBlock();;

  LoopStatement() {
    super();
  }

  @Override
  public LoopStatement ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public LoopStatement init(Object assignment) {
    if (assignment instanceof AssignmentStatement) {
      setInitStatement((AssignmentStatement) assignment);
      return this;
    }
    throw cantConvert("assignment", assignment);
  }

  public LoopStatement condition(Object expression) {
    if (expression instanceof ExpressionStatement) {
      setConditionStatement((ExpressionStatement) expression);
      return this;
    }
    throw cantConvert("expression", expression);
  }

  public LoopStatement post(Object statement) {
    if (statement instanceof GoloStatement) {
      setPostStatement((GoloStatement) statement);
      return this;
    }
    throw cantConvert("statement", statement);
  }

  public LoopStatement block(Block innerBlock) {
    setBlock(innerBlock);
    return this;
  }

  public LoopStatement block(Object... statements) {
    return this.block(Builders.block(statements));
  }

  public boolean hasInitStatement() {
    return initStatement != null;
  }

  public AssignmentStatement getInitStatement() {
    return initStatement;
  }

  public void setInitStatement(AssignmentStatement init) {
    this.initStatement = init;
    makeParentOf(init);
  }

  public ExpressionStatement getConditionStatement() {
    return conditionStatement;
  }

  public void setConditionStatement(ExpressionStatement cond) {
    conditionStatement = (cond == null ? constant(false) : cond);
    makeParentOf(conditionStatement);
  }

  public Block getBlock() {
    return block;
  }

  public void setBlock(Block block) {
    this.block = (block == null ? Block.emptyBlock() : block);
    makeParentOf(this.block);
  }

  public GoloStatement getPostStatement() {
    return postStatement;
  }

  public void setPostStatement(GoloStatement stat) {
    postStatement = stat;
    makeParentOf(postStatement);
  }

  public boolean hasPostStatement() {
    return postStatement != null;
  }

  @Override
  public void relink(ReferenceTable table) {
    block.relink(table);
  }

  @Override
  public void relinkTopLevel(ReferenceTable table) {
    block.relinkTopLevel(table);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    if (initStatement != null) {
      initStatement.accept(visitor);
    }
    conditionStatement.accept(visitor);
    if (postStatement != null) {
      postStatement.accept(visitor);
    }
    block.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (Objects.equals(initStatement, original)) {
      init(newElement);
    } else if (Objects.equals(conditionStatement, original)) {
      condition(newElement);
    } else if (Objects.equals(postStatement, original)) {
      post(newElement);
    } else if (Objects.equals(block, original)) {
      block((Block) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
