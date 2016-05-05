/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Objects;
import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class ThrowStatement extends GoloStatement {

  private GoloStatement expressionStatement;

  ThrowStatement(GoloStatement expressionStatement) {
    super();
    setExpressionStatement(expressionStatement);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitThrowStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    expressionStatement.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (Objects.equals(original, expressionStatement) && newElement instanceof ExpressionStatement) {
      setExpressionStatement((ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement stat) {
    this.expressionStatement = stat;
    makeParentOf(stat);
  }

  @Override
  public ThrowStatement ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }
}
