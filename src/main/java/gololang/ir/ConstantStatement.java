/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import static gololang.Messages.message;

/**
 * A constant value.
 */
public final class ConstantStatement extends ExpressionStatement<ConstantStatement> {

  private Object value;

  private ConstantStatement(Object value) {
    super();
    this.value = value;
  }

  /**
   * Creates a constant value.
   *
   * <p>
   */
  public static ConstantStatement of(Object o) {
    if (o instanceof ConstantStatement) {
      return (ConstantStatement) o;
    }
    if (o instanceof Class || o instanceof ClassReference) {
      return new ConstantStatement(ClassReference.of(o));
    }
    if (!isLiteralValue(o)) {
      throw new IllegalArgumentException("Not a constant value: " + o);
    }
    return new ConstantStatement(o);
  }

  public static boolean isLiteralValue(Object v) {
    return v == null
        || v instanceof String
        || v instanceof Character
        || v instanceof Number
        || v instanceof Boolean
        || v instanceof Class || v instanceof ClassReference
        || v instanceof FunctionRef;
  }

  public Object value() {
    return value;
  }

  public ConstantStatement value(Object v) {
    this.value = v;
    return this;
  }

  protected ConstantStatement self() { return this; }

  /**
   * {@inheritDoc}
   *
   * <p>Always throws an exception since {@link NamedArgument} can't have a local declaration.
   */
  @Override
  public ConstantStatement with(Object a) {
    throw new UnsupportedOperationException(message("invalid_local_definition", this.getClass().getName()));
  }

  @Override
  public boolean hasLocalDeclarations() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("`%s`", value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConstantStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
