/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.Objects;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import static java.util.Collections.unmodifiableList;

class StructDocumentation implements DocumentationElement, MemberHolder {

  private String name;
  private String documentation;
  private int line;
  private List<MemberDocumentation> members = new LinkedList<>();
  private DocumentationElement parent;

  public String type() {
    return "struct";
  }

  public String name() {
    return name;
  }

  public StructDocumentation name(String n) {
    name = n;
    return this;
  }

  public DocumentationElement parent() {
    return parent;
  }

  public StructDocumentation parent(DocumentationElement p) {
    parent = p;
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
    line = l;
    return this;
  }

  public List<MemberDocumentation> members() {
    return unmodifiableList(members);
  }

  public StructDocumentation members(Collection<MemberDocumentation> m) {
    members.addAll(m);
    return this;
  }

  public MemberDocumentation addMember(String name) {
    MemberDocumentation doc = new MemberDocumentation().name(name);
    members.add(doc);
    return doc;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) { return false; }
    if (o == this) { return true; }
    if (!(o instanceof StructDocumentation)) { return false; }
    StructDocumentation that = (StructDocumentation) o;
    return this.name.equals(that.name)
        && this.members.equals(that.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.members);
  }

}

