/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import java.util.List;
import java.util.LinkedList;

import static java.util.Collections.unmodifiableList;
import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class CaseStatement extends GoloStatement implements Alternatives<Block> {

  private Block otherwise;
  private final LinkedList<WhenClause<Block>> clauses = new LinkedList<>();

  CaseStatement() {
    super();
  }

  public CaseStatement when(Object cond) {
    WhenClause<Block> clause = new WhenClause<Block>((ExpressionStatement) cond, null);
    this.clauses.add(clause);
    makeParentOf(clause);
    return this;
  }

  public CaseStatement then(Object action) {
    this.clauses.getLast().setAction((Block) action);
    return this;
  }

  public CaseStatement otherwise(Object action) {
    otherwise = (Block) action;
    makeParentOf(otherwise);
    return this;
  }

  public List<WhenClause<Block>> getClauses() {
    return unmodifiableList(this.clauses);
  }

  public Block getOtherwise() {
    return this.otherwise;
  }

  @Override
  public CaseStatement ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCaseStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (WhenClause<Block> clause : clauses) {
      clause.accept(visitor);
    }
    otherwise.accept(visitor);
  }

  @Override
  public void replaceElement(GoloElement original, GoloElement newElement) {
    if (!(newElement instanceof Block || newElement instanceof WhenClause)) {
      throw cantConvert("Block or WhenClause", newElement);
    }
    if (otherwise.equals(original)) {
      otherwise(newElement);
      return;
    }
    if (clauses.contains(original)) {
      @SuppressWarnings("unchecked")
      WhenClause<Block> when = (WhenClause<Block>) newElement;
      clauses.set(clauses.indexOf(original), when);
      makeParentOf(when);
      return;
    }
    throw doesNotContain(original);
  }
}
