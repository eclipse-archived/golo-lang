/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.List;
import java.util.LinkedList;

/**
 * A destructuring assignment.
 *
 * <p>For instance:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * let a, b = [1, 2]
 * </code></pre>
 */
public final class DestructuringAssignment extends GoloAssignment<DestructuringAssignment> {

  private final List<LocalReference> references = new LinkedList<>();
  private boolean isVarargs = false;

  private DestructuringAssignment() {
    super();
  }

  /**
   * Create a destructuring assignment.
   *
   * <p>Typical usage:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * destruct(tuple(1, 2)).to("a", "b").declaring(true)
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let a, b = [1, 2]
   * </code></pre>
   *
   * @param expr the expression to destructure
   * @see #as(Object)
   */
  public static DestructuringAssignment destruct(Object expr) {
    return new DestructuringAssignment().as(expr);
  }

  /**
   * Creates a uninitialized destructuring assignment.
   */
  public static DestructuringAssignment create() {
    return new DestructuringAssignment();
  }

  protected DestructuringAssignment self() { return this; }

  /**
   * Checks if this destructuring is a varargs one.
   *
   * @see #varargs(boolean)
   */
  public boolean isVarargs() {
    return this.isVarargs;
  }

  /**
   * Defines if this destructuring is a varargs one.
   *
   * <p>For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let a, b... = list[1, 2, 3, 4]
   * </code></pre>
   * <p>This is a builder method.
   */
  public DestructuringAssignment varargs(boolean varargs) {
    this.isVarargs = varargs;
    return this;
  }

  /**
   * Defines this destructuring as a varargs one.
   *
   * <p>Same as {@code varargs(true)}.
   */
  public DestructuringAssignment varargs() {
    return varargs(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DestructuringAssignment variable() {
    for (LocalReference ref : references) {
      ref.variable();
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConstant() {
    if (!references.isEmpty()) {
      return references.get(0).isConstant();
    }
    return false;
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
      references.add(LocalReference.of(o));
    }
    return this;
  }

  @Override
  public String toString() {
    List<String> names = new LinkedList<>();
    for (LocalReference r : getReferences()) {
      names.add(r.toString());
    }
    return String.join(", ", names) + " = " + expression().toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitDestructuringAssignment(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    for (LocalReference ref : references) {
      ref.accept(visitor);
    }
    super.walk(visitor);
  }
}
