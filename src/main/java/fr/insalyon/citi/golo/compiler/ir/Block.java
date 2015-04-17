/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class Block extends ExpressionStatement {

  private final List<GoloStatement> statements = new LinkedList<>();
  private ReferenceTable referenceTable;

  private boolean hasReturn = false;

  public Block(ReferenceTable referenceTable) {
    super();
    this.referenceTable = referenceTable;
  }

  public ReferenceTable getReferenceTable() {
    return referenceTable;
  }

  public void internReferenceTable() {
    this.referenceTable = referenceTable.flatDeepCopy(true);
  }

  public List<GoloStatement> getStatements() {
    return unmodifiableList(statements);
  }

  public void addStatement(GoloStatement statement) {
    statements.add(statement);
    checkForReturns(statement);
  }

  public void prependStatement(GoloStatement statement) {
    statements.add(0, statement);
    checkForReturns(statement);
  }

  private void checkForReturns(GoloStatement statement) {
    if ((statement instanceof ReturnStatement) || (statement instanceof ThrowStatement)) {
      hasReturn = true;
    } else if (statement instanceof ConditionalBranching) {
      hasReturn = hasReturn || ((ConditionalBranching) statement).returnsFromBothBranches();
    }
  }

  public boolean hasReturn() {
    return hasReturn;
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitBlock(this);
  }
}
