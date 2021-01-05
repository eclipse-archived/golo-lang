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

import static java.util.Objects.requireNonNull;

/**
 * Empty IR node.
 */
public final class Noop extends GoloStatement<Noop> implements ToplevelGoloElement {

  private final String comment;

  private Noop(String comment) {
    this.comment = requireNonNull(comment);
  }

  public static Noop of(Object comment) {
    return new Noop(comment == null ? "" : comment.toString());
  }

  protected Noop self() { return this; }

  public String comment() {
    return this.comment;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNoop(this);
  }

  @Override
  public void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
