/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import static gololang.Messages.message;

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

  @Override
  public ExpressionStatement where(Object a) {
    throw new UnsupportedOperationException(message("invalid_local_definition", this.getClass().getName()));
  }

  @Override
  public boolean hasLocalDeclarations() {
    return false;
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
