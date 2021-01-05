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

import org.eclipse.golo.compiler.PackageAndClass;

public final class UnionValue extends TypeWithMembers<UnionValue> {

  public UnionValue(String name) {
    super(name);
  }

  protected UnionValue self() { return this; }

  /**
   * {@inheritDoc}
   */
  @Override
  public PackageAndClass getPackageAndClass() {
    Union u = ancestorOfType(Union.class);
    if (u == null) {
      return PackageAndClass.of(getName());
    }
    return u.getPackageAndClass().createInnerClass(getName());
  }

  public Union getUnion() {
    return ancestorOfType(Union.class);
  }

  protected String getFactoryDelegateName() {
    return getUnion().getPackageAndClass().toString() + "." + getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnionValue(this);
  }
}

