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

public final class ModuleImport extends GoloElement<ModuleImport> {

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

  protected ModuleImport self() { return this; }

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
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
