/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

public class ASTAnonymousFunctionInvocation extends GoloASTNode {

  private boolean constant;

  private boolean onExpression;

  public ASTAnonymousFunctionInvocation(int id) {
    super(id);
  }

  public ASTAnonymousFunctionInvocation(GoloParser p, int id) {
    super(p, id);
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public boolean isConstant() {
    return constant;
  }

  public boolean isOnExpression() {
    return onExpression;
  }

  public void setOnExpression(boolean onExpression) {
    this.onExpression = onExpression;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return String.format("ASTAnonymousFunctionInvocation{constant=%s, onExpression=%s}",
        constant,
        onExpression);
  }
}
