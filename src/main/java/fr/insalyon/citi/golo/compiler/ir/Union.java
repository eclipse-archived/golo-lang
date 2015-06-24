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

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import static java.util.Collections.unmodifiableSet;

public final class Union {

  public static final class Value {
    private final String name;
    private final Union union;
    private final PackageAndClass packageAndClass;
    private final Set<String> members = new LinkedHashSet<>();

    public Value(Union union, String name) {
      this.name = name;
      this.union = union;
      this.packageAndClass = union.getPackageAndClass().createInnerClass(name);
    }

    public PackageAndClass getPackageAndClass() {
      return packageAndClass;
    }

    public Union getUnion() {
      return union;
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
  }

  private final PackageAndClass packageAndClass;
  private final Set<Value> values = new LinkedHashSet<>();

  public Union(PackageAndClass packageAndClass) {
    this.packageAndClass = packageAndClass;
  }

  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  public void addValue(String name, Collection<String> members) {
    Value value = new Value(this, name);
    value.addMembers(members);
    values.add(value);
  }

  public Collection<Value> getValues() {
    return unmodifiableSet(this.values);
  }
}
