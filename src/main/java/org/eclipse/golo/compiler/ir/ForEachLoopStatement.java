/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.List;
import java.util.LinkedList;
import java.util.Objects;
import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;

public final class ForEachLoopStatement extends GoloStatement implements Scope, BlockContainer {
  private Block block = Block.emptyBlock();
  private ExpressionStatement iterable;
  private final List<LocalReference> valueRefs = new LinkedList<>();
  private ExpressionStatement whenClause;
  private boolean isVarargs = false;

  ForEachLoopStatement() {
    super();
  }

  public ForEachLoopStatement block(Object block) {
    this.block = Builders.toBlock(block);
    return this;
  }

  @Override
  public ForEachLoopStatement ofAST(GoloASTNode node) {
    node.setIrElement(this);
    return this;
  }

  public ForEachLoopStatement on(Object iterable) {
    this.iterable = ExpressionStatement.of(iterable);
    return this;
  }

  public ForEachLoopStatement varargs(boolean b) {
    this.isVarargs = b;
    return this;
  }

  public ForEachLoopStatement var(Object varRef) {
    LocalReference ref;
    if (varRef instanceof String) {
      ref = Builders.localRef(varRef).variable();
    } else if (varRef instanceof LocalReference) {
      ref = (LocalReference) varRef;
    } else {
      throw new IllegalArgumentException("not a ref, a string or a builder");
    }
    this.valueRefs.add(ref);
    return this;
  }

  public ForEachLoopStatement when(Object clause) {
    this.whenClause = ExpressionStatement.of(clause);
    return this;
  }

  public ExpressionStatement getIterable() {
    return iterable;
  }

  public Block getBlock() {
    return block;
  }

  public boolean isDestructuring() {
    return valueRefs.size() > 1;
  }

  public boolean isVarargs() {
    return this.isVarargs;
  }

  public LocalReference getReference() {
    return valueRefs.get(0);
  }

  public List<LocalReference> getReferences() {
    return unmodifiableList(valueRefs);
  }

  public boolean hasWhenClause() {
    return whenClause != null;
  }

  public ExpressionStatement getWhenClause() {
    return whenClause;
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
    visitor.visitForEachLoopStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (LocalReference ref : valueRefs) {
      ref.accept(visitor);
    }
    iterable.accept(visitor);
    if (whenClause != null) {
      whenClause.accept(visitor);
    }
    block.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (Objects.equals(iterable, original)) {
      on(newElement);
    } else if (Objects.equals(whenClause, original)) {
      when(newElement);
    } else if (Objects.equals(block, original)) {
      block(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
