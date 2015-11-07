/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.List;
import java.util.LinkedList;
import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class CollectionLiteral extends ExpressionStatement {

  public static enum Type {
    array, list, set, map, tuple, vector, range
  }

  private final Type type;
  private final List<ExpressionStatement> expressions = new LinkedList<>();

  CollectionLiteral(Type type) {
    super();
    this.type = type;
  }

  @Override
  public CollectionLiteral ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public CollectionLiteral add(Object expression) {
    ExpressionStatement expr = ExpressionStatement.of(expression);
    this.expressions.add(expr);
    makeParentOf(expr);
    return this;
  }

  public Type getType() {
    return type;
  }

  public List<ExpressionStatement> getExpressions() {
    return expressions;
  }

  @Override
  public String toString() {
    return this.type.toString() + this.expressions.toString();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCollectionLiteral(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (ExpressionStatement expression : expressions) {
      expression.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expressions.contains(original) && newElement instanceof ExpressionStatement) {
      expressions.set(expressions.indexOf(original), (ExpressionStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
