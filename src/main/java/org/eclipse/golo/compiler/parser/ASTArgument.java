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

public class ASTArgument extends GoloASTNode {

  private String name;
  private boolean named;

  public ASTArgument(int id) {
    super(id);
  }

  public ASTArgument(GoloParser p, int id) {
    super(p, id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isNamed() {
    return named;
  }

  public void setNamed(boolean named) {
    this.named = named;
  }

  @Override
  public String toString() {
    return String.format("ASTArgument{name=%s, named=%s}", name, named);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}
