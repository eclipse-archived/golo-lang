/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.PackageAndClass;
import org.eclipse.golo.compiler.parser.GoloASTNode;


public final class UnionValue extends TypeWithMembers {

  UnionValue(Union union, String name) {
    super(name);
    setParentNode(union);
  }

  @Override
  public UnionValue ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  @Override
  public PackageAndClass getPackageAndClass() {
    return getUnion().getPackageAndClass().createInnerClass(getName());
  }

  public Union getUnion() {
    return (Union) getParentNode().get();
  }

  protected String getFactoryDelegateName() {
    return getUnion().getPackageAndClass().toString() + "." + getName();
  }

  @Override
  protected void setParentNode(GoloElement parent) {
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

