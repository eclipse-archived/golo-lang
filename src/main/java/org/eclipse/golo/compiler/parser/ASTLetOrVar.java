/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

public class ASTLetOrVar extends GoloASTNode implements NamedNode {

  public static enum Type {
    LET, VAR
  }

  private Type type;
  private String name;
  private boolean moduleState = false;

  public ASTLetOrVar(int id) {
    super(id);
  }

  public ASTLetOrVar(GoloParser p, int id) {
    super(p, id);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public boolean isModuleState() {
    return moduleState;
  }

  public void setModuleState(boolean moduleState) {
    this.moduleState = moduleState;
  }

  @Override
  public String toString() {
    return String.format("ASTLetOrVar{type=%s, name='%s', moduleState=%s}",
        type,
        name,
        moduleState);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
