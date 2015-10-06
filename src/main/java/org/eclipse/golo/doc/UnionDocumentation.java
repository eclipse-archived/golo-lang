/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import static java.util.Collections.unmodifiableSet;

class UnionDocumentation implements Comparable<UnionDocumentation>, DocumentationElement {

  public static final class UnionValueDocumentation implements DocumentationElement {
    private String name;
    private String documentation;
    private int line;
    private Set<String> members = new LinkedHashSet<>();

    public String name() {
      return name;
    }

    public UnionValueDocumentation name(String n) {
      name = n;
      return this;
    }

    public String documentation() {
      return (documentation != null ? documentation : "\n");
    }

    public UnionValueDocumentation documentation(String doc) {
      documentation = doc;
      return this;
    }

    public int line() {
      return line;
    }

    public UnionValueDocumentation line(int l) {
      line = l;
      return this;
    }

    public Set<String> members() {
      return unmodifiableSet(members);
    }

    public UnionValueDocumentation members(Collection<String> m) {
      members.addAll(m);
      return this;
    }

    public boolean hasMembers() {
      return !members.isEmpty();
    }
  }

  private String name;
  private String documentation;
  private int line;
  private Set<UnionValueDocumentation> values = new LinkedHashSet<>();

  public String name() {
    return name;
  }

  public UnionDocumentation name(String name) {
    this.name = name;
    return this;
  }

  public String documentation() {
    return (documentation != null ? documentation : "\n");
  }

  public UnionDocumentation documentation(String doc) {
    documentation = doc;
    return this;
  }

  public int line() {
    return line;
  }

  public UnionDocumentation line(int l) {
    line = l;
    return this;
  }

  public Set<UnionValueDocumentation> values() {
    return unmodifiableSet(values);
  }

  public UnionDocumentation values(Collection<UnionValueDocumentation> v) {
    values.addAll(v);
    return this;
  }

  public boolean addValue(UnionValueDocumentation v) {
    return values.add(v);
  }

  public UnionValueDocumentation addValue(String name) {
    UnionValueDocumentation v = new UnionValueDocumentation();
    v.name(name);
    values.add(v);
    return v;
  }

  @Override
  public int compareTo(UnionDocumentation o) {
    return name.compareTo(o.name());
  }

}
