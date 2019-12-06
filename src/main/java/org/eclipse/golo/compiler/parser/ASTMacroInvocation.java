/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

public class ASTMacroInvocation extends GoloASTNode implements NamedNode {

  private String name;
  private boolean topLevel = false;

  public ASTMacroInvocation(int id) {
    super(id);
  }

  public ASTMacroInvocation(GoloParser p, int id) {
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

  public void setTopLevel(boolean v) {
    topLevel = v;
  }

  public boolean isTopLevel() {
    return topLevel;
  }

  @Override
  public String toString() {
    return "ASTMacroInvocation{"
            + "name='" + name + "'"
            + (topLevel ? ", top-level" : "")
            + '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}
