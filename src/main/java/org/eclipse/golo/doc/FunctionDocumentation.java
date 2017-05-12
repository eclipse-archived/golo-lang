/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.List;
import java.util.LinkedList;

class FunctionDocumentation implements DocumentationElement {

  private String name;
  private int line;
  private String documentation;
  private List<String> arguments = new LinkedList<>();
  private boolean augmentation = false;
  private boolean varargs = false;
  private boolean local = false;
  private DocumentationElement parent;

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return local ? "local function" : "function";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return name;
  }

  public FunctionDocumentation name(String v) {
    name = v;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String label() {
    StringBuilder sig = new StringBuilder(this.name());
    sig.append("(");
    boolean first = true;
    for (String arg : this.arguments()) {
      if (!first) {
        sig.append(", ");
      }
      sig.append(arg);
      first = false;
    }
    if (this.varargs()) {
      sig.append("...");
    }
    sig.append(")");
    return sig.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String documentation() {
    return (documentation != null ? documentation : "");
  }

  public FunctionDocumentation documentation(String v) {
    documentation = v;
    return this;
  }

  public List<String> arguments() {
    return arguments;
  }

  public String argument(int i) {
    return arguments.get(i);
  }

  public FunctionDocumentation arguments(List<String> v) {
    arguments.addAll(v);
    return this;
  }

  public int arity() {
    return arguments.size();
  }

  public boolean augmentation() {
    return augmentation;
  }

  public FunctionDocumentation augmentation(boolean v) {
    augmentation = v;
    return this;
  }

  public boolean varargs() {
    return varargs;
  }

  public FunctionDocumentation varargs(boolean v) {
    varargs = v;
    return this;
  }

  public boolean local() {
    return local;
  }

  public FunctionDocumentation local(boolean v) {
    local = v;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return line;
  }

  public FunctionDocumentation line(int l) {
    line = l;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String id() {
    StringBuilder id = new StringBuilder();
    if (augmentation) {
      id.append(parent.id());
      id.append(".");
    }
    id.append(name());
    id.append("_");
    id.append(arguments().size());
    if (varargs()) {
      id.append("v");
    }
    return id.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return parent;
  }

  public FunctionDocumentation parent(DocumentationElement p) {
    parent = p;
    return this;
  }

  @Override
  public int compareTo(DocumentationElement other) {
    if (this == other) {
      return 0;
    }
    if (null == other) {
      return 1;
    }
    if (other instanceof FunctionDocumentation) {
      FunctionDocumentation o = (FunctionDocumentation) other;
      int c = name.compareToIgnoreCase(o.name);
      if (c == 0) {
        c = arity() < o.arity() ? -1 : 1;
      }
      return c;
    }
    return -1 * other.compareTo(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunctionDocumentation that = (FunctionDocumentation) o;

    if (line != that.line) return false;
    if (augmentation != that.augmentation) return false;
    if (varargs != that.varargs) return false;
    if (local != that.local) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (documentation != null ? !documentation.equals(that.documentation) : that.documentation != null) return false;
    if (arguments != null ? !arguments.equals(that.arguments) : that.arguments != null) return false;
    return parent != null ? parent.equals(that.parent) : that.parent == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + line;
    result = 31 * result + (documentation != null ? documentation.hashCode() : 0);
    result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
    result = 31 * result + (augmentation ? 1 : 0);
    result = 31 * result + (varargs ? 1 : 0);
    result = 31 * result + (local ? 1 : 0);
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }
}
