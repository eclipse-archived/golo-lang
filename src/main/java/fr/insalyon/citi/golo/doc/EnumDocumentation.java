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

class EnumDocumentation implements Comparable<EnumDocumentation>, DocumentationElement {

  public static final class EnumValueDocumentation implements DocumentationElement {
    private String name;
    private String documentation;
    private int line;
    private Set<String> members = new LinkedHashSet<>();

    public String name() {
      return name;
    }

    public EnumValueDocumentation name(String n) {
      name = n;
      return this;
    }

    public String documentation() {
      return (documentation != null ? documentation : "\n");
    }

    public EnumValueDocumentation documentation(String doc) {
      documentation = doc;
      return this;
    }

    public int line() {
      return line;
    }

    public EnumValueDocumentation line(int l) {
      line = l;
      return this;
    }

    public Set<String> members() {
      return unmodifiableSet(members);
    }

    public EnumValueDocumentation members(Collection<String> m) {
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
  private Set<EnumValueDocumentation> values = new LinkedHashSet<>();

  public String name() {
    return name;
  }

  public EnumDocumentation name(String name) {
    this.name = name;
    return this;
  }

  public String documentation() {
    return (documentation != null ? documentation : "\n");
  }

  public EnumDocumentation documentation(String doc) {
    documentation = doc;
    return this;
  }

  public int line() {
    return line;
  }

  public EnumDocumentation line(int l) {
    line = l;
    return this;
  }

  public Set<EnumValueDocumentation> values() {
    return unmodifiableSet(values);
  }

  public EnumDocumentation values(Collection<EnumValueDocumentation> v) {
    values.addAll(v);
    return this;
  }

  public boolean addValue(EnumValueDocumentation v) {
    return values.add(v);
  }

  public EnumValueDocumentation addValue(String name) {
    EnumValueDocumentation v = new EnumValueDocumentation();
    v.name(name);
    values.add(v);
    return v;
  }

  @Override
  public int compareTo(EnumDocumentation o) {
    return name.compareTo(o.name());
  }

}
