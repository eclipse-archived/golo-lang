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

public class CollectionLiteral extends ExpressionStatement {

  public static enum Type {
    array, list, set, map, tuple, vector
  }

  private final Type type;
  private final List<ExpressionStatement> expressions;

  public CollectionLiteral(Type type, List<ExpressionStatement> expressions) {
    this.type = type;
    this.expressions = expressions;
  }

  public Type getType() {
    return type;
  }

  public List<ExpressionStatement> getExpressions() {
    return expressions;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitCollectionLiteral(this);
  }
}
