/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.List;
import java.util.LinkedList;


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
    return (documentation != null ? documentation : "") ;
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
    int c = name.compareTo(o.name);
    if (c == 0) {
      return arity() < o.arity() ? -1 : 1;
    }
    return c;
  }
}
