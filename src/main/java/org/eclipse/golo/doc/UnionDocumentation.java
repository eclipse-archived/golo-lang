/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import static java.util.Collections.unmodifiableList;

class UnionDocumentation implements DocumentationElement {

  public static final class UnionValueDocumentation implements DocumentationElement, MemberHolder {
    private String name;
    private String documentation;
    private int line;
    private List<MemberDocumentation> members = new LinkedList<>();
    private DocumentationElement parent;

    public String type() {
      return "union value";
    }

    public String name() {
      return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fullName() {
      return parent.fullName() + '$' + name;
    }

    public String label() {
      return name;
    }

    public DocumentationElement parent() {
      return parent;
    }

    public UnionValueDocumentation parent(DocumentationElement p) {
      parent = p;
      return this;
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

    public List<MemberDocumentation> members() {
      return unmodifiableList(members);
    }

    public UnionValueDocumentation members(Collection<MemberDocumentation> m) {
      members.addAll(m);
      return this;
    }

    public MemberDocumentation addMember(String name) {
      MemberDocumentation doc = new MemberDocumentation().name(name);
      members.add(doc);
      return doc;
    }
  }

  private String name;
  private String documentation;
  private int line;
  private List<UnionValueDocumentation> values = new LinkedList<>();
  private DocumentationElement parent;

  public String type() {
    return "union";
  }

  public String name() {
    return name;
  }

  public UnionDocumentation name(String name) {
    this.name = name;
    return this;
  }

  public DocumentationElement parent() {
    return parent;
  }

  public UnionDocumentation parent(DocumentationElement p) {
    parent = p;
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

  public List<UnionValueDocumentation> values() {
    return unmodifiableList(values);
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
    v.parent(this);
    v.name(name);
    values.add(v);
    return v;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) { return false; }
    if (o == this) { return true; }
    if (!(o instanceof UnionDocumentation)) { return false; }
    return this.name.equals(((UnionDocumentation) o).name);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

}
