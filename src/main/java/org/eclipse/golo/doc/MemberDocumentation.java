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
  // TODO: display the default value. Hard: need to pretty-print code!
  private String name;
  private String documentation;
  private String defaultRepresentation;
  private int line;

  public String name() {
    return name;
  }

  public MemberDocumentation name(String n) {
    name = n;
    return this;
  }

  public String documentation() {
    return (documentation != null ? documentation : "\n");
  }

  public MemberDocumentation documentation(String doc) {
    documentation = doc;
    return this;
  }

  public int line() {
    return line;
  }

  public MemberDocumentation line(int l) {
    line = l;
    return this;
  }

  public boolean hasDefault() {
    return defaultRepresentation != null && !"".equals(defaultRepresentation);
  }

  public String defaultRepresentation() {
    return defaultRepresentation;
  }

  public MemberDocumentation defaultRepresentation(String repr) {
    defaultRepresentation = repr;
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
