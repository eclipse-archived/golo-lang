/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.PackageAndClass;

public final class ModuleImport extends GoloElement {

  private final PackageAndClass packageAndClass;

  public ModuleImport(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }


  @Override
  public String toString() {
    return "ModuleImport{" +
        "packageAndClass=" + packageAndClass +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleImport that = (ModuleImport) o;

    if (!packageAndClass.equals(that.packageAndClass)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageAndClass.hashCode();
    return result;
  }
}
