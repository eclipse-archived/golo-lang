/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.doc;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import static java.util.Collections.unmodifiableSet;

class StructDocumentation implements Comparable<StructDocumentation>, DocumentationElement {

  private String name;
  private String documentation;
  private int line;
  private Set<String> members = new LinkedHashSet<>();

  public String name() {
    return name;
  }

  public StructDocumentation name(String n) {
    name = n;
    return this;
  }

  public String documentation() {
    return (documentation != null ? documentation : "\n");
  }

  public StructDocumentation documentation(String doc) {
    documentation = doc;
    return this;
  }

  public int line() {
    return line;
  }

  public StructDocumentation line(int l) {
    line= l;
    return this;
  }

  public Set<String> members() {
    return unmodifiableSet(members);
  }

  public StructDocumentation members(Collection<String> m) {
    members.addAll(m);
    return this;
  }

  @Override
  public int compareTo(StructDocumentation o) {
    return name.compareTo(o.name());
  }
}

