/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import java.util.ArrayList;
import java.util.List;

public class ASTInvocationExpression extends GoloASTNode {

  private final List<String> operators = new ArrayList<>();

  public ASTInvocationExpression(int id) {
    super(id);
  }

  public ASTInvocationExpression(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public List<String> getOperators() {
    return operators;
  }

  public void addOperator(String symbol) {
    operators.add(symbol);
  }

  @Override
  public String toString() {
    return String.format("ASTInvocationExpression{operators=%s}", operators);
  }
}
