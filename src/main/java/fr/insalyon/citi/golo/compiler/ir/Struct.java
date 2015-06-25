/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.PackageAndClass;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Struct {

  private final PackageAndClass packageAndClass;
  private final Set<String> members;
  private final Set<String> publicMembers;

  public Struct(PackageAndClass packageAndClass, Set<String> members) {
    this.packageAndClass = packageAndClass;
    this.members = members;
    this.publicMembers = new LinkedHashSet<>();
    for (String member : members) {
      if (!member.startsWith("_")) {
        publicMembers.add(member);
      }
    }
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public Set<String> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public Set<String> getPublicMembers() {
    return Collections.unmodifiableSet(publicMembers);
  }
}
