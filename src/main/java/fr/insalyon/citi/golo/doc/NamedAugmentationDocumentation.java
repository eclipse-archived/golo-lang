/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;

class NamedAugmentationDocumentation extends AbstractSet<FunctionDocumentation> implements Comparable<NamedAugmentationDocumentation>, DocumentationElement {

  private String name;
  private String documentation;
  private int line;
  private Set<FunctionDocumentation> functions = new TreeSet<>();

  public String name() { return name; }

  public NamedAugmentationDocumentation name(String name){
    this.name = name;
    return this;
  }

  public String documentation() { 
    return (documentation != null ? documentation : "");
  }

  public NamedAugmentationDocumentation documentation(String documentation){
    this.documentation = documentation;
    return this;
  }

  public int line() { return line; }

  public NamedAugmentationDocumentation line(int line){
    this.line = line;
    return this;
  }

  public Set<FunctionDocumentation> functions() {
    return unmodifiableSet(functions);
  }

  public NamedAugmentationDocumentation functions(Collection<FunctionDocumentation> docs) {
    this.functions.addAll(docs);
    return this;
  }

  @Override
  public boolean add(FunctionDocumentation func) {
    return this.functions.add(func);
  }

  @Override
  public int size() { return functions.size(); }

  @Override
  public Iterator<FunctionDocumentation> iterator() { return functions.iterator(); }

  @Override 
  public int compareTo(NamedAugmentationDocumentation o) {
    return name().compareTo(o.name());
  }
}

