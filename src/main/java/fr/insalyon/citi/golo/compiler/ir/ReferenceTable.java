/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.compiler.ir;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

public final class ReferenceTable {

  private ReferenceTable parent;
  private final Map<String, LocalReference> table = new HashMap<>();

  public ReferenceTable() {
    this(null);
  }

  private ReferenceTable(ReferenceTable parent) {
    this.parent = parent;
  }

  public ReferenceTable add(LocalReference reference) {
    table.put(reference.getName(), reference);
    return this;
  }

  public boolean hasReferenceFor(String name) {
    return table.containsKey(name) || (parent != null && parent.hasReferenceFor(name));
  }

  public LocalReference get(String name) {
    LocalReference reference = table.get(name);
    if (reference != null) {
      return reference;
    }
    if (parent != null) {
      return parent.get(name);
    }
    return null;
  }

  public Set<String> ownedSymbols() {
    return unmodifiableSet(table.keySet());
  }

  public Collection<LocalReference> ownedReferences() {
    return unmodifiableCollection(table.values());
  }

  public void relink(ReferenceTable parent) {
    this.parent = parent;
  }

  public Set<String> symbols() {
    HashSet<String> localSymbols = new HashSet<>(table.keySet());
    if (parent != null) {
      localSymbols.addAll(parent.symbols());
    }
    return localSymbols;
  }

  public Collection<LocalReference> references() {
    Collection<LocalReference> localReferences = new HashSet<>(table.values());
    if (parent != null) {
      localReferences.addAll(parent.references());
    }
    return localReferences;
  }

  public ReferenceTable fork() {
    return new ReferenceTable(this);
  }

  public ReferenceTable flatDeepCopy(boolean turnIntoConstants) {
    ReferenceTable referenceTable = new ReferenceTable();
    Collection<LocalReference> parentRefs = (parent != null) ? parent.references() : Collections.<LocalReference>emptySet();
    for (LocalReference reference : references()) {
      if (turnIntoConstants && parentRefs.contains(reference) && !table.containsValue(reference)) {
        referenceTable.add(new LocalReference(LocalReference.Kind.CONSTANT, reference.getName()));
      } else {
        referenceTable.add(new LocalReference(reference.getKind(), reference.getName()));
      }
    }
    return referenceTable;
  }

  public void remove(String name) {
    table.remove(name);
  }
}
