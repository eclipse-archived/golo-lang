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

public final class AssignmentStatement extends GoloAssignment<AssignmentStatement> {

  private LocalReference localReference;

  AssignmentStatement() { super(); }

  protected AssignmentStatement self() { return this; }

  public LocalReference getLocalReference() {
    return localReference;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalReference[] getReferences() {
    if (localReference != null) {
      return new LocalReference[]{localReference};
    }
    return new LocalReference[0];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReferencesCount() {
    return localReference == null ? 0 : 1;
  }

  /**
   * @inheritDoc
   */
  @Override
  public AssignmentStatement to(Object... refs) {
    if (refs.length != 1 || refs[0] == null) {
      throw new IllegalArgumentException("Must assign to one reference");
    }
    this.localReference = makeParentOf((LocalReference) refs[0]);
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s = %s", localReference, getExpressionStatement().toString());
  }

  /**
   * @inheritDoc
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }

}
