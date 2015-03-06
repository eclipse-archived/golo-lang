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

