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

import org.eclipse.golo.compiler.parser.GoloASTNode;

public final class AssignmentStatement extends GoloAssignment {

  private LocalReference localReference;

  AssignmentStatement() { super(); }

  /**
   * @inheritDoc
   */
  @Override
  public AssignmentStatement ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public AssignmentStatement declaring() {
    return this.declaring(true);
  }

  @Override
  public AssignmentStatement declaring(boolean isDeclaring) {
    super.declaring(isDeclaring);
    return this;
  }

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
    localReference = (LocalReference) refs[0];
    makeParentOf(localReference);
    return this;
  }

  /**
   * @inheritDoc
   */
  @Override
  public AssignmentStatement as(Object expr) {
    super.as(expr);
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

  /**
   * @inheritDoc
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    super.walk(visitor);
  }

}
