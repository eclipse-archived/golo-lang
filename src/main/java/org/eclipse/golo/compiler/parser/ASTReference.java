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

public class ASTReference extends GoloASTNode implements NamedNode {

  private String name;

  public ASTReference(int id) {
    super(id);
  }

  public ASTReference(GoloParser p, int id) {
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

  @Override
  public String toString() {
    return String.format("ASTReference{name='%s'}", name);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
