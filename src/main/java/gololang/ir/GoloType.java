/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import org.eclipse.golo.compiler.PackageAndClass;

public abstract class GoloType<T extends GoloType<T>> extends GoloElement<T> implements NamedElement {

  private final String name;

  GoloType(String name) {
    super();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  String getFullName() {
    return getPackageAndClass().toString();
  }

  public PackageAndClass getPackageAndClass() {
    GoloModule m = enclosingModule();
    if (m == null) {
      return PackageAndClass.of(getName());
    }
    return m.getTypesPackage().createSubPackage(getName());
  }

}
