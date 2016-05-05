/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.List;
import java.util.LinkedList;
import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;

public class CollectionComprehension extends ExpressionStatement {

  private final CollectionLiteral.Type type;
  private ExpressionStatement expression;
  private final List<Block> loopBlocks = new LinkedList<>();

  CollectionComprehension(CollectionLiteral.Type type) {
    super();
    this.type = type;
  }

  @Override
  public CollectionComprehension ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  public CollectionComprehension expression(Object expression) {
    this.expression = (ExpressionStatement) expression;
    makeParentOf(this.expression);
    return this;
  }

  public CollectionComprehension loop(Object b) {
    Block theBlock = Block.of(b);
    this.loopBlocks.add(theBlock);
    makeParentOf(theBlock);
    return this;
  }

  public ExpressionStatement getExpression() {
    return this.expression;
  }

  public List<Block> getLoopBlocks() {
    return unmodifiableList(loopBlocks);
  }

  public CollectionLiteral.Type getType() {
    return this.type;
  }

  public CollectionLiteral.Type getMutableType() {
    return (CollectionLiteral.Type.tuple.equals(type)
       || CollectionLiteral.Type.array.equals(type))
      ? CollectionLiteral.Type.list
      : type;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCollectionComprehension(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expression.accept(visitor);
    for (Block block: loopBlocks) {
      block.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expression == original && newElement instanceof ExpressionStatement) {
      expression(newElement);
    } else if (newElement instanceof Block && loopBlocks.contains(original)) {
      loopBlocks.set(loopBlocks.indexOf(original), (Block) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
