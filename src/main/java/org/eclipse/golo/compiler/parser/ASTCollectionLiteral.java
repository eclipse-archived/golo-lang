/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
