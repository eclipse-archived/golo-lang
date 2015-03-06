/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.doc;

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
