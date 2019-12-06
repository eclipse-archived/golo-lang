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

/**
 * An import of a Golo or Java module, class or package.
 *
 * <p>Represents golo code such as
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * import java.util.Collections
 * </code></pre>
 */
public final class ModuleImport extends GoloElement<ModuleImport> implements ToplevelGoloElement {

  private final PackageAndClass packageAndClass;
  private final boolean implicit;

  private ModuleImport(PackageAndClass packageAndClass, boolean implicit) {
    super();
    this.packageAndClass = packageAndClass;
    this.implicit = implicit;
  }

  /**
   * Create an implicit module import.
   *
   * <p>
   * Implicit imports are ones automatically added to a module, contrary to ones added by the developer.
   * If the given name is already a module import, it is returned unchanged if already implicit; a new one is created
   * otherwise.
   * The name to import is derived using {@link PackageAndClass#of(Object)}.
   */
  public static ModuleImport implicit(Object name) {
    if (name instanceof ModuleImport) {
      ModuleImport mod = (ModuleImport) name;
      if (mod.implicit) {
        return mod;
      }
      return new ModuleImport(mod.packageAndClass, true);
    }
    return new ModuleImport(PackageAndClass.of(name), true);
  }

  /**
   * Create a module import.
   *
   * <p>
   * If the given name is already a module import, it is returned unchanged.
   * The name to import is derived using {@link PackageAndClass#of(Object)}.
   */
  public static ModuleImport of(Object name) {
    if (name instanceof ModuleImport) {
      return (ModuleImport) name;
    }
    return new ModuleImport(PackageAndClass.of(name), false);
  }

  protected ModuleImport self() { return this; }

  /**
   * Returns the {@link PackageAndClass} of the module to be imported.
   */
  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  /**
   * Checks if this import is implicit.
   * <p>
   * Implicit imports are ones automatically added to a module, contrary to ones added by the developer.
   */
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
   * <p>
   * Equality ignores the implicit status.
   * Therefore, adding explicitly a import that is already implicit is a noop.
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
