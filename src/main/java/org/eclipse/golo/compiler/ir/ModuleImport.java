/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;
import org.eclipse.golo.compiler.PackageAndClass;

public final class ModuleImport extends GoloElement {

  private final PackageAndClass packageAndClass;
  private final boolean implicit;

  ModuleImport(PackageAndClass packageAndClass, boolean implicit) {
    super();
    this.packageAndClass = packageAndClass;
    this.implicit = implicit;
  }

  ModuleImport(PackageAndClass packageAndClass) {
    this(packageAndClass, false);
  }

  @Override
  public ModuleImport ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public boolean isImplicit() {
    return this.implicit;
  }

  @Override
  public String toString() {
    return "ModuleImport{"
        + "packageAndClass=" + packageAndClass
        + (implicit ? ", implicit" : "")
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    ModuleImport that = (ModuleImport) o;
    return packageAndClass.equals(that.packageAndClass);
  }

  @Override
  public int hashCode() {
    return packageAndClass.hashCode();
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitModuleImport(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // nothing to do, not a composite
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }
}
