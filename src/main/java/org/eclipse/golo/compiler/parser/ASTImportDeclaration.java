/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import java.util.List;
import java.util.Collections;

public class ASTImportDeclaration extends GoloASTNode implements NamedNode {

  private String name;
  private boolean relative;
  private List<String> multi = Collections.emptyList();

  public ASTImportDeclaration(int i) {
    super(i);
  }

  public ASTImportDeclaration(GoloParser p, int i) {
    super(p, i);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void setRelative(boolean b) {
    this.relative = b;
  }

  public boolean isRelative() {
    return this.relative;
  }

  public List<String> getMultiple() {
    return multi;
  }

  public void setMultiple(List<String> names) {
    multi = names;
  }

  @Override
  public String toString() {
    return String.format("ASTImportDeclaration{name='%s'}", name);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
