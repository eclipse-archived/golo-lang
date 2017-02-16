/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;
import static java.util.Collections.unmodifiableList;

public abstract class ExpressionStatement extends GoloStatement {

  private final LinkedList<GoloAssignment> declarations = new LinkedList<>();

  public ExpressionStatement where(Object a) {
    if (!(a instanceof GoloAssignment)) {
      throw new IllegalArgumentException(("Must be an assignment, got " + a));
    }
    GoloAssignment declaration = (GoloAssignment) a;
    declarations.add(declaration.declaring());
    makeParentOf(declaration);
    return this;
  }

  public GoloAssignment[] declarations() {
    return declarations.toArray(new GoloAssignment[declarations.size()]);
  }

  public boolean hasLocalDeclarations() {
    return !declarations.isEmpty();
  }

  public void clearDeclarations() {
    declarations.clear();
  }

  public static ExpressionStatement of(Object expr) {
    if (expr instanceof ExpressionStatement) {
      return (ExpressionStatement) expr;
    }
    throw cantConvert("ExpressionStatement", expr);
  }
}
