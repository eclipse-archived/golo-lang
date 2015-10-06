/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public class ThrowStatement extends GoloStatement {

  private final GoloStatement expressionStatement;

  public ThrowStatement(GoloStatement expressionStatement) {
    super();
    this.expressionStatement = expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitThrowStatement(this);
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }
}
