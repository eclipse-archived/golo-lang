/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

import java.util.List;
import java.util.LinkedList;

public class ASTForEachLoop extends GoloASTNode {

  private String elementIdentifier;
  private final List<String> names = new LinkedList<>();
  private boolean isVarargs = false;

  public ASTForEachLoop(int id) {
    super(id);
  }

  public ASTForEachLoop(GoloParser p, int id) {
    super(p, id);
  }

  public String getElementIdentifier() {
    return elementIdentifier;
  }

  public void setElementIdentifier(String elementIdentifier) {
    this.elementIdentifier = elementIdentifier;
  }

  public List<String> getNames() {
    return this.names;
  }

  public void setNames(List<String> names) {
    this.names.clear();
    this.names.addAll(names);
  }

  public void setVarargs(boolean b) {
    this.isVarargs = b;
  }

  public boolean isVarargs() {
    return this.isVarargs;
  }

  @Override
  public String toString() {
    return String.format("ASTForEachLoop{elementIdentifier='%s'}", elementIdentifier);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
