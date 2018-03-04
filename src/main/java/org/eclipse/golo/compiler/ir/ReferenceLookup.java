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

public final class ReferenceLookup extends ExpressionStatement<ReferenceLookup> {

  private final String name;

  ReferenceLookup(String name) {
    super();
    this.name = name;
  }

  protected ReferenceLookup self() { return this; }

  public String getName() {
    return name;
  }

  public LocalReference resolveIn(ReferenceTable referenceTable) {
    return referenceTable.get(name);
  }

  public LocalReference varRef() {
    return Builders.localRef(name).variable();
  }

  public LocalReference letRef() {
    return Builders.localRef(name);
  }

  @Override
  public String toString() {
    return String.format("Ref{name=%s}", getName());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReferenceLookup(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // nothing to do, not a composite
  }

  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }

}
