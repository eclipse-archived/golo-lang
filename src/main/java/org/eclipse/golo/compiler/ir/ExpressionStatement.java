/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedList;

public abstract class ExpressionStatement extends GoloStatement {

  private final LinkedList<GoloAssignment> declarations = new LinkedList<>();

  /**
   * Defines a variable declaration (assignment) local to this expression.
   */
  public ExpressionStatement with(Object a) {
    if (!(a instanceof GoloAssignment)) {
      throw new IllegalArgumentException(("Must be an assignment, got " + a));
    }
    GoloAssignment declaration = (GoloAssignment) a;
    declarations.add(declaration.declaring());
    makeParentOf(declaration);
    return this;
  }

  /**
   * Returns the local declarations of this expression if any.
   */
  public GoloAssignment[] declarations() {
    return declarations.toArray(new GoloAssignment[declarations.size()]);
  }

  /**
   * Checks if this expression has local variable declarations.
   */
  public boolean hasLocalDeclarations() {
    return !declarations.isEmpty();
  }

  /**
   * Removes all local declarations.
   */
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
