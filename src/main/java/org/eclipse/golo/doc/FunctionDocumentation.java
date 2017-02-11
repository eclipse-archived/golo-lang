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
import java.util.Objects;

class FunctionDocumentation implements Comparable<FunctionDocumentation>, DocumentationElement {

  private String name;
  private int line;
  private String documentation;
  private List<String> arguments = new LinkedList<>();
  private boolean augmentation = false;
  private boolean varargs = false;
  private boolean local = false;

  public String name() {
    return name;
  }

  public FunctionDocumentation name(String v) {
    name = v;
    return this;
  }

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

  public int line() {
    return line;
  }

  public FunctionDocumentation line(int l) {
    line = l;
    return this;
  }

  @Override
  public int compareTo(FunctionDocumentation o) {
    if (this.equals(o)) { return 0; }
    int c = name.compareTo(o.name);
    if (c == 0) {
      return arity() < o.arity() ? -1 : 1;
    }
    return c;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) { return false; }
    if (other == this) { return true; }
    if (!(other instanceof FunctionDocumentation)) { return false; }
    FunctionDocumentation that = (FunctionDocumentation) other;
    return this.name.equals(that.name)
        && this.local == that.local
        && this.varargs == that.varargs
        && this.augmentation == that.augmentation
        && this.arity() == that.arity();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.local, this.varargs, this.augmentation, this.arity());
  }
}
