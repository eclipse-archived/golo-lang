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

import org.eclipse.golo.compiler.PackageAndClass;

public final class UnionValue extends TypeWithMembers<UnionValue> {

  UnionValue(Union union, String name) {
    super(name);
    setParentNode(union);
  }

  protected UnionValue self() { return this; }

  @Override
  public PackageAndClass getPackageAndClass() {
    return getUnion().getPackageAndClass().createInnerClass(getName());
  }

  public Union getUnion() {
    return (Union) parent();
  }

  protected String getFactoryDelegateName() {
    return getUnion().getPackageAndClass().toString() + "." + getName();
  }

  @Override
  protected void setParentNode(GoloElement<?> parent) {
    if (!(parent instanceof Union)) {
      throw new IllegalArgumentException("UnionValue can only be defined in a Union");
    }
    super.setParentNode(parent);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnionValue(this);
  }
}

