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

public class ASTCollectionLiteral extends GoloASTNode {

  private String type;
  private boolean isComprehension = false;

  public ASTCollectionLiteral(int id) {
    super(id);
  }

  public ASTCollectionLiteral(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setComprehension(boolean v) {
    this.isComprehension = v;
  }

  public boolean isComprehension() {
    return this.isComprehension;
  }

  @Override
  public String toString() {
    return String.format("ASTCollectionLiteral{type='%s'}", type);
  }
}
