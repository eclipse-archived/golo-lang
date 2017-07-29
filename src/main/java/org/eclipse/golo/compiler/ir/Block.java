/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.golo.compiler.ir.Builders.*;
import static java.util.Objects.requireNonNull;

public final class Block extends ExpressionStatement {
  private final List<GoloStatement> statements = new LinkedList<>();
  private ReferenceTable referenceTable;
  private boolean hasReturn = false;

  Block(ReferenceTable referenceTable) {
    super();
    this.referenceTable = referenceTable;
  }

  public static Block emptyBlock() {
    return new Block(new ReferenceTable());
  }

  public static Block of(Object block) {
    if (block == null) {
      return emptyBlock();
    }
    if (block instanceof Block) {
      return (Block) block;
    }
    throw cantConvert("Block", block);
  }

  @Override
  public Block ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  public void merge(Block other) {
    for (GoloStatement innerStatement : other.getStatements()) {
      this.addStatement(innerStatement);
    }
  }

  public ReferenceTable getReferenceTable() {
    return referenceTable;
  }

  @Override
  public Optional<ReferenceTable> getLocalReferenceTable() {
    return Optional.of(referenceTable);
  }

  public Block ref(Object referenceTable) {
    if (referenceTable instanceof ReferenceTable) {
      setReferenceTable((ReferenceTable) referenceTable);
      return this;
    }
    throw new IllegalArgumentException("not a reference table");
  }

  public void setReferenceTable(ReferenceTable referenceTable) {
    this.referenceTable = requireNonNull(referenceTable);
  }

  public void internReferenceTable() {
    this.referenceTable = referenceTable.flatDeepCopy(true);
  }

  public List<GoloStatement> getStatements() {
    return unmodifiableList(statements);
  }

  public Block add(Object statement) {
    this.addStatement(toGoloStatement(statement));
    return this;
  }

  private void updateStateWith(GoloStatement statement) {
    referenceTable.updateFrom(statement);
    makeParentOf(statement);
    checkForReturns(statement);
  }

  public void addStatement(GoloStatement statement) {
    statements.add(statement);
    updateStateWith(statement);
  }

  public void prependStatement(GoloStatement statement) {
    statements.add(0, statement);
    updateStateWith(statement);
  }

  private void setStatement(int idx, GoloStatement statement) {
    statements.set(idx, statement);
    updateStateWith(statement);
  }

  private void checkForReturns(GoloStatement statement) {
    if (statement instanceof ReturnStatement || statement instanceof ThrowStatement) {
      hasReturn = true;
    } else if (statement instanceof ConditionalBranching) {
      hasReturn = hasReturn || ((ConditionalBranching) statement).returnsFromBothBranches();
    }
  }

  public boolean hasReturn() {
    return hasReturn;
  }

  public int size() {
    return statements.size();
  }

  public boolean hasOnlyReturn() {
    return statements.size() == 1
           && statements.get(0) instanceof ReturnStatement
           && !((ReturnStatement) statements.get(0)).isReturningVoid();
  }

  @Override
  public String toString() {
    return "{" + statements.toString() + "}";
  }

  public boolean isEmpty() {
    return statements.isEmpty();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBlock(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (LocalReference ref : referenceTable.ownedReferences()) {
      ref.accept(visitor);
    }
    for (GoloStatement statement : statements) {
      statement.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (statements.contains(original) && newElement instanceof GoloStatement) {
      setStatement(statements.indexOf(original), (GoloStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
