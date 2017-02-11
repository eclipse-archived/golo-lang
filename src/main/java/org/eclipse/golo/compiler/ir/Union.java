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

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public final class Union extends GoloElement {

  private PackageAndClass moduleName;
  private final String name;
  private final Set<UnionValue> values = new LinkedHashSet<>();

  Union(String name) {
    super();
    this.name = name;
  }

  @Override
  public Union ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public String getName() {
    return name;
  }

  public PackageAndClass getPackageAndClass() {
    return moduleName.createSubPackage("types").createSubPackage(name);
  }

  public void setModuleName(PackageAndClass module) {
    this.moduleName = module;
  }

  public UnionValue createValue(String name) {
    return new UnionValue(this, name);
  }

  public boolean addValue(UnionValue value) {
    makeParentOf(value);
    return values.add(value);
  }

  public Collection<UnionValue> getValues() {
    return unmodifiableSet(values);
  }

  public Union value(String name, Member... members) {
    UnionValue value = new UnionValue(this, name);
    value.addMembers(asList(members));
    values.add(value);
    return this;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnion(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (UnionValue value : getValues()) {
      value.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    if (values.contains(original) && newElement instanceof UnionValue) {
      values.remove(original);
      addValue((UnionValue) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

}
