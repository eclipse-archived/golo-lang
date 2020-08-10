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

import static java.util.Objects.requireNonNull;

public final class Member extends GoloElement<Member> {

  private final String name;

  private Member(String name) {
    super();
    this.name = requireNonNull(name);
  }

  public static Member of(Object o) {
    if (o instanceof Member) {
      return (Member) o;
    }
    return new Member(o.toString());
  }

  protected Member self() { return this; }

  public String getName() {
    return name;
  }

  public boolean isPublic() {
    return !name.startsWith("_");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMember(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace(original, newElement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("<%s>", name);
  }
}
