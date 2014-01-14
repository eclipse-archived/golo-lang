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
