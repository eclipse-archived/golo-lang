/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import java.util.LinkedHashSet;

public class ASTStructDeclaration extends GoloASTNode implements NamedNode {

  private String name;
  private LinkedHashSet<String> members;

  public ASTStructDeclaration(int id) {
    super(id);
  }

  public ASTStructDeclaration(GoloParser p, int id) {
    super(p, id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LinkedHashSet<String> getMembers() {
    return members;
  }

  public void setMembers(LinkedHashSet<String> members) {
    this.members = members;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return String.format("ASTStructDeclaration{name='%s', members=%s}", name, members);
  }
}
