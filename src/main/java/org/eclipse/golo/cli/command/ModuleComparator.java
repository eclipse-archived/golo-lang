/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import java.util.Comparator;
import java.util.Set;
import gololang.ir.GoloModule;

class ModuleComparator implements Comparator<GoloModule> {

  @Override
  public int compare(GoloModule m1, GoloModule m2) {
    if (m1.hasMacros() && !m2.hasMacros()) { return -1; }
    if (!m1.hasMacros() && m2.hasMacros()) { return 1; }
    Set<String> m1Used = m1.getUsedModules();
    Set<String> m2Used = m2.getUsedModules();
    if (m1Used.isEmpty() && !m2Used.isEmpty()) { return -1; }
    if (!m1Used.isEmpty() && m2Used.isEmpty()) { return 1; }
    if (m1Used.contains(m2.getPackageAndClass().toString())) { return 1; }
    if (m2Used.contains(m1.getPackageAndClass().toString())) { return -1; }
    if (m1.getImports().stream().anyMatch((mi) -> mi.getPackageAndClass().equals(m2.getPackageAndClass()))) { return 1; }
    if (m2.getImports().stream().anyMatch((mi) -> mi.getPackageAndClass().equals(m1.getPackageAndClass()))) { return -1; }
    return 0;
  }

}
