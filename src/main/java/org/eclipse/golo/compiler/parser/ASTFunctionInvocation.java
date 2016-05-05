/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

public class ASTFunctionInvocation extends GoloASTNode implements NamedNode {

  private String name;

  private boolean constant;

  public ASTFunctionInvocation(int id) {
    super(id);
  }

  public ASTFunctionInvocation(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public boolean isConstant() {
    return constant;
  }

  @Override
  public String toString() {
    return String.format("ASTFunctionInvocation{name='%s', constant=%s}", name, constant);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
