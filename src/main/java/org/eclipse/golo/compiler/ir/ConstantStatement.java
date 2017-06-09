/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import static gololang.Messages.message;

import org.eclipse.golo.compiler.parser.GoloASTNode;

public class ConstantStatement extends ExpressionStatement {

  private Object value;

  ConstantStatement(Object value) {
    super();
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object v) {
    value = v;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Always throws an exception since {@link NamedArgument} can't have a local declaration.
   */
  @Override
  public ExpressionStatement with(Object a) {
    throw new UnsupportedOperationException(message("invalid_local_definition", this.getClass().getName()));
  }

  @Override
  public boolean hasLocalDeclarations() {
    return false;
  }

  @Override
  public ConstantStatement ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConstantStatement(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // nothing to do, not a composite
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }

}
