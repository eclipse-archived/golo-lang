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
import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;

public class ASTDestructuringAssignment extends GoloASTNode {

  private ASTLetOrVar.Type type;
  private List<String> names = new ArrayList<>();
  private boolean isVarargs = false;

  public ASTDestructuringAssignment(int id) {
    super(id);
  }

  public ASTDestructuringAssignment(GoloParser p, int id) {
    super(p, id);
  }

  public ASTLetOrVar.Type getType() {
    return type;
  }

  public void setType(ASTLetOrVar.Type type) {
    this.type = type;
  }

  public List<String> getNames() {
    return unmodifiableList(names);
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
    return String.format("ASTDestructuringAssignment{type=%s, names=%s, varargs=%s}",
           type, names, isVarargs);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
