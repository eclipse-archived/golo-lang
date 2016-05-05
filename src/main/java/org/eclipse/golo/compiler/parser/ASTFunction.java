/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import java.util.List;

public class ASTFunction extends GoloASTNode {

  private List<String> parameters;
  private boolean varargs = false;
  private boolean compactForm = false;

  public ASTFunction(int i) {
    super(i);
  }

  public ASTFunction(GoloParser p, int i) {
    super(p, i);
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  public boolean isCompactForm() {
    return compactForm;
  }

  public void setCompactForm(boolean compactForm) {
    this.compactForm = compactForm;
  }

  @Override
  public String toString() {
    return String.format("ASTFunction{parameters=%s, varargs=%s, compactForm=%s}",
        parameters,
        varargs,
        compactForm);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
