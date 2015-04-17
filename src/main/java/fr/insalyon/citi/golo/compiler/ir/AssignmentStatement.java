/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public class AssignmentStatement extends GoloStatement {

  private LocalReference localReference;
  private final ExpressionStatement expressionStatement;
  private boolean declaring = false;

  public AssignmentStatement(LocalReference localReference, ExpressionStatement expressionStatement) {
    super();
    this.localReference = localReference;
    this.expressionStatement = expressionStatement;
  }

  public boolean isDeclaring() {
    return declaring;
  }

  public void setDeclaring(boolean declaring) {
    this.declaring = declaring;
  }

  public LocalReference getLocalReference() {
    return localReference;
  }

  public void setLocalReference(LocalReference localReference) {
    this.localReference = localReference;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }
}
