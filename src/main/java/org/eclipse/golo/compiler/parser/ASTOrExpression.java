/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

public class ASTOrExpression extends GoloASTNode {

  public ASTOrExpression(int id) {
    super(id);
  }

  public ASTOrExpression(GoloParser p, int id) {
    super(p, id);
  }

  private int count = 0;

  public int count() {
    return count;
  }

  public void increaseCount() {
    count += 1;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return "ASTOrExpression{}";
  }
}
