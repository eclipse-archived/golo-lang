/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

public class ASTWhileLoop extends GoloASTNode {

  public ASTWhileLoop(int id) {
    super(id);
  }

  public ASTWhileLoop(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTWhileLoop{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
