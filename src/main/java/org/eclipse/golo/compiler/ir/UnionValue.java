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
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;

import static java.util.Collections.unmodifiableSet;

public final class UnionValue extends GoloElement {
  private final String name;
  private final Set<String> members = new LinkedHashSet<>();

  UnionValue(Union union, String name) {
    this.name = name;
    setParentNode(union);
  }

  public PackageAndClass getPackageAndClass() {
    return getUnion().getPackageAndClass().createInnerClass(name);
  }

  public Union getUnion() {
    return (Union) getParentNode().get();
  }

  public String getName() {
    return name;
  }

  public void addMembers(Collection<String> memberNames) {
    this.members.addAll(memberNames);
  }

  public boolean hasMembers() {
    return !this.members.isEmpty();
  }

  public Set<String> getMembers() {
    return unmodifiableSet(members);
  }

  @Override
  protected void setParentNode(GoloElement parent) {
    if (!(parent instanceof Union)) {
      throw new IllegalArgumentException("UnionValue can only be defined in a Union");
    }
    super.setParentNode(parent);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnionValue(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // do nothing, not a composite.
  }
}

