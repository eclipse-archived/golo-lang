/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import java.util.List;
import java.util.LinkedList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class DestructuringAssignment extends GoloStatement {
  private final List<LocalReference> references = new LinkedList<>();
  private boolean isVarargs = false;
  private boolean isDeclaring = false;
  private ExpressionStatement expression;

  DestructuringAssignment() {
    super();
  }

  @Override
  public DestructuringAssignment ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  public boolean isVarargs() {
    return this.isVarargs;
  }

  public DestructuringAssignment varargs(boolean varargs) {
    this.isVarargs = varargs;
    return this;
  }

  public DestructuringAssignment varargs() {
    return varargs(true);
  }

  public DestructuringAssignment declaring() {
    return declaring(true);
  }

  public DestructuringAssignment declaring(boolean d) {
    this.isDeclaring = d;
    return this;
  }

  public boolean isDeclaring() {
    return this.isDeclaring;
  }

  public ExpressionStatement getExpression() {
    return this.expression;
  }

  public DestructuringAssignment expression(Object expr) {
    this.expression = ExpressionStatement.of(expr);
    makeParentOf(expression);
    return this;
  }

  public List<LocalReference> getReferences() {
    return unmodifiableList(this.references);
  }

  public DestructuringAssignment to(Object var) {
    requireNonNull(var);
    if (var instanceof Iterable) {
      for (Object o : (Iterable) var) {
        this.to(o);
      }
    } else if (var instanceof LocalReference) {
      references.add((LocalReference) var);
    } else {
      throw new IllegalArgumentException("LocalReference expected, got a " + var.getClass());
    }
    return this;
  }

  @Override
  public String toString() {
    List<String> names = new LinkedList<>();
    for (LocalReference r : getReferences()) {
      names.add(r.toString());
    }
    return String.join(", ", names) + " = " + getExpression().toString();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitDestructuringAssignment(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (LocalReference ref : references) {
      ref.accept(visitor);
    }
    expression.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (expression == original && newElement instanceof ExpressionStatement) {
      expression(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
