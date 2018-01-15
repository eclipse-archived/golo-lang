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

import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;

public final class MatchExpression extends ExpressionStatement implements Alternatives<ExpressionStatement> {

  private ExpressionStatement otherwise;
  private final LinkedList<WhenClause<ExpressionStatement>> clauses = new LinkedList<>();

  MatchExpression() {
    super();
  }

  public MatchExpression when(Object cond) {
    WhenClause<ExpressionStatement> clause = new WhenClause<ExpressionStatement>(ExpressionStatement.of(cond), null);
    this.clauses.add(clause);
    makeParentOf(clause);
    return this;
  }

  public MatchExpression then(Object action) {
    this.clauses.getLast().setAction((ExpressionStatement) action);
    return this;
  }

  public MatchExpression otherwise(Object action) {
    otherwise = (ExpressionStatement) action;
    makeParentOf(otherwise);
    return this;
  }

  public List<WhenClause<ExpressionStatement>> getClauses() {
    return unmodifiableList(this.clauses);
  }

  public ExpressionStatement getOtherwise() {
    return this.otherwise;
  }

  @Override
  public MatchExpression ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMatchExpression(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (WhenClause<ExpressionStatement> clause : clauses) {
      clause.accept(visitor);
    }
    otherwise.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (!(newElement instanceof ExpressionStatement || newElement instanceof WhenClause)) {
      throw cantConvert("ExpressionStatement or WhenClause", newElement);
    }
    if (otherwise.equals(original)) {
      otherwise(newElement);
      return;
    }
    if (clauses.contains(original)) {
      @SuppressWarnings("unchecked")
      WhenClause<ExpressionStatement> when = (WhenClause<ExpressionStatement>) newElement;
      clauses.set(clauses.indexOf(original), when);
      makeParentOf(when);
      return;
    }
    throw doesNotContain(original);
  }
}
