/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

/**
 * Assignment statement in Golo code.
 * <p>
 * An assignment is a statement like:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * var foo = 42
 * let bar = a(more: complex()) + expression
 * </code></pre>
 */
public final class AssignmentStatement extends GoloAssignment<AssignmentStatement> implements ToplevelGoloElement {
  private LocalReference localReference;

  AssignmentStatement() { super(); }

  /**
   * Full assignment creation in one call.
   * <p>
   * A lot less readable than the fluent API, but useful when doing meta-generation
   *
   * @param ref the reference to assign to ({@link AssignmentStatement#to(Object...)}).
   * @param value the value to assign ({@link AssignmentStatement#as(Object)}).
   * @param declaring whether the assignment is a declaring one.
   * @return an initialized assignment element.
   */
  public static AssignmentStatement create(Object ref, Object value, Object declaring) {
    return new AssignmentStatement().to(ref).as(value).declaring((boolean) declaring);
  }

  /**
   * Creates a uninitialized assignment.
   */
  public static AssignmentStatement create() {
    return new AssignmentStatement();
  }

  protected AssignmentStatement self() { return this; }

  public LocalReference getLocalReference() {
    return localReference;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AssignmentStatement variable() {
    this.localReference.variable();
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConstant() {
    return this.localReference.isConstant();
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
   * {@inheritDoc}
   */
  @Override
  public AssignmentStatement to(Object... refs) {
    if (refs.length != 1 || refs[0] == null) {
      throw new IllegalArgumentException("Must assign to one reference");
    }
    this.localReference = makeParentOf(LocalReference.of(refs[0]));
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s = %s", localReference, expression().toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }
}
