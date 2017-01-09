/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class AssignmentStatement extends GoloStatement {
  private LocalReference localReference;
  private ExpressionStatement expressionStatement;
  private boolean declaring = false;

  AssignmentStatement() { super(); }

  /**
   * @inheritDoc
   */
  @Override
  public AssignmentStatement ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public boolean isDeclaring() {
    return declaring;
  }

  public AssignmentStatement declaring() {
    return declaring(true);
  }

  public AssignmentStatement declaring(boolean isDeclaring) {
    this.declaring = isDeclaring;
    return this;
  }

  public LocalReference getLocalReference() {
    return localReference;
  }

  /**
   * Defines the reference to assign to.
   */
  public AssignmentStatement to(Object ref) {
    localReference = (LocalReference) ref;
    makeParentOf(localReference);
    return this;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  /**
   * Defines the value to be assigned.
   */
  public AssignmentStatement as(Object expr) {
    expressionStatement = (ExpressionStatement) expr;
    makeParentOf(expressionStatement);
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s = %s", localReference, expressionStatement);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }

  /**
   * @inheritDoc
   */
  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (original.equals(expressionStatement) && newElement instanceof ExpressionStatement) {
      as(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
