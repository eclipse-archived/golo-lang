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

class MemberDocumentation implements DocumentationElement {
  private String name;
  private String documentation;
  private int line;
  private DocumentationElement parent;

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return name;
  }

  public MemberDocumentation name(String n) {
    name = n;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String documentation() {
    return (documentation != null ? documentation : "\n");
  }

  public MemberDocumentation documentation(String doc) {
    documentation = doc;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return line;
  }

  public MemberDocumentation line(int l) {
    line = l;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return "member";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return parent;
  }

  public MemberDocumentation parent(DocumentationElement p) {
    parent = p;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) { return false; }
    if (o == this) { return true; }
    if (!(o instanceof MemberDocumentation)) { return false; }
    MemberDocumentation that = (MemberDocumentation) o;
    return this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }


}
