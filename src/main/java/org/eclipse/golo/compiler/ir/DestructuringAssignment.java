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

import java.util.List;
import java.util.LinkedList;

public final class DestructuringAssignment extends GoloAssignment<DestructuringAssignment> {

  private final List<LocalReference> references = new LinkedList<>();
  private boolean isVarargs = false;

  DestructuringAssignment() {
    super();
  }

  protected DestructuringAssignment self() { return this; }

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

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalReference[] getReferences() {
    return this.references.toArray(new LocalReference[references.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReferencesCount() {
    return this.references.size();
  }

  /**
   * @inheritDoc
   */
  @Override
  public DestructuringAssignment to(Object... refs) {
    for (Object o : refs) {
      if (o instanceof LocalReference) {
        references.add((LocalReference) o);
      } else {
        throw new IllegalArgumentException("LocalReference expected, got a " + o.getClass().getName());
      }
    }
    return this;
  }


  @Override
  public String toString() {
    List<String> names = new LinkedList<>();
    for (LocalReference r : getReferences()) {
      names.add(r.toString());
    }
    return String.join(", ", names) + " = " + getExpressionStatement().toString();
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
    super.walk(visitor);
  }
}
