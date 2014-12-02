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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;

import static java.util.Collections.unmodifiableSortedSet;

class AugmentationDocumentation extends AbstractSet<FunctionDocumentation> implements SortedSet<FunctionDocumentation> {

  private String target;
  private String documentation;
  private int line;
  private SortedSet<FunctionDocumentation> functions = new TreeSet<>();

  public String target() { return target; }

  public AugmentationDocumentation target(String target){
    this.target = target;
    return this;
  }

  public String documentation() {
    return (documentation != null) ? documentation : "\n";
  }

  public AugmentationDocumentation documentation(String documentation){
    if (documentation != null) {
      this.documentation = documentation;
    }
    return this;
  }

  public int line() { return line; }

  public AugmentationDocumentation line(int line){
    this.line = line;
    return this;
  }

  public SortedSet<FunctionDocumentation> functions() {
    return unmodifiableSortedSet(functions);
  }

  public AugmentationDocumentation functions(Collection<FunctionDocumentation> docs) {
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
  public FunctionDocumentation last() { return functions.last(); }

  @Override
  public FunctionDocumentation first() { return functions.first(); }

  @Override
  public SortedSet<FunctionDocumentation> headSet(FunctionDocumentation e) {
    return functions.headSet(e);
  }

  @Override
  public SortedSet<FunctionDocumentation> tailSet(FunctionDocumentation e) {
    return functions.tailSet(e);
  }

  @Override
  public SortedSet<FunctionDocumentation> subSet(FunctionDocumentation f, FunctionDocumentation t) {
    return functions.subSet(f, t);
  }

  @Override
  public Comparator<? super FunctionDocumentation> comparator() {
    return functions.comparator();
  }
}
