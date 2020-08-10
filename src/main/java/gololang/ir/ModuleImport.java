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

import org.eclipse.golo.compiler.PackageAndClass;

public final class ModuleImport extends GoloElement<ModuleImport> implements ToplevelGoloElement {

  private final PackageAndClass packageAndClass;
  private final boolean implicit;

  private ModuleImport(PackageAndClass packageAndClass, boolean implicit) {
    super();
    this.packageAndClass = packageAndClass;
    this.implicit = implicit;
  }

  public static ModuleImport implicit(Object name) {
    return new ModuleImport(PackageAndClass.of(name), true);
  }

  public static ModuleImport of(Object name) {
    if (name instanceof ModuleImport) {
      return (ModuleImport) name;
    }
    return new ModuleImport(PackageAndClass.of(name), false);
  }

  protected ModuleImport self() { return this; }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public boolean isImplicit() {
    return this.implicit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ModuleImport{"
        + "packageAndClass=" + packageAndClass
        + (implicit ? ", implicit" : "")
        + '}';
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    ModuleImport that = (ModuleImport) o;
    return packageAndClass.equals(that.packageAndClass);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return packageAndClass.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitModuleImport(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
