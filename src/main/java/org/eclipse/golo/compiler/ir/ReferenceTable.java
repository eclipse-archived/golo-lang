/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static org.eclipse.golo.compiler.ir.Builders.localRef;

public final class ReferenceTable {

  private ReferenceTable parent;
  private final Map<String, LocalReference> table = new LinkedHashMap<>();

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

  public int size() {
    return table.size() + (parent != null ? parent.size() : 0);
  }

  public boolean hasReferenceFor(String name) {
    return table.containsKey(name) || parent != null && parent.hasReferenceFor(name);
  }

  public void updateFrom(GoloStatement statement) {
    if (statement instanceof ReferencesHolder) {
      for (LocalReference r : ((ReferencesHolder) statement).getDeclaringReferences()) {
        this.add(r);
      }
    }
  }

  public LocalReference get(String name) {
    LocalReference reference = table.get(name);
    if (reference != null) {
      return reference;
    }
    if (parent != null && parent != this) {
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

  public void relink(ReferenceTable parent, boolean prune) {
    if (parent == this) { return; }
    if (prune) {
      for (LocalReference reference : parent.references()) {
        if (this.hasReferenceFor(reference.getName())) {
          this.remove(reference.getName());
        }
      }
    }
    this.parent = parent;
  }

  public void relink(ReferenceTable parent) {
    relink(parent, true);
  }

  private boolean isLinkedTo(ReferenceTable other) {
    if (this != other && this.parent == null) {
      return false;
    }
    return this == other || this.parent == other || this.parent.isLinkedTo(other);
  }

  public void relinkTopLevel(ReferenceTable topLevel) {
    if (this == topLevel) { return; }
    if (this.parent == null) {
      this.parent = topLevel;
    } else if (!this.isLinkedTo(topLevel) && !topLevel.isLinkedTo(this)) {
      this.parent.relinkTopLevel(topLevel);
    }
  }

  public Set<String> symbols() {
    LinkedHashSet<String> localSymbols = new LinkedHashSet<>(table.keySet());
    if (parent != null) {
      localSymbols.addAll(parent.symbols());
    }
    return localSymbols;
  }

  public Collection<LocalReference> references() {
    Collection<LocalReference> localReferences = new LinkedHashSet<>(table.values());
    if (parent != null) {
      for (LocalReference ref : parent.references()) {
        if (!table.containsKey(ref.getName())) {
          localReferences.add(ref);
        }
      }
    }
    return localReferences;
  }

  public ReferenceTable fork() {
    return new ReferenceTable(this);
  }

  public ReferenceTable flatDeepCopy(boolean turnIntoConstants) {
    ReferenceTable referenceTable = new ReferenceTable();
    Set<String> tableSymbols = ownedSymbols();
    for (LocalReference reference : references()) {
      String refName = reference.getName();
      if (reference.isModuleState()) {
        referenceTable.add(localRef(refName).kind(reference.getKind()));
        continue;
      }
      if (turnIntoConstants && !tableSymbols.contains(refName)) {
        referenceTable.add(localRef(refName).synthetic(reference.isSynthetic()));
      } else {
        referenceTable.add(localRef(refName)
            .kind(reference.getKind())
            .synthetic(reference.isSynthetic()));
      }
    }
    return referenceTable;
  }

  public void remove(String name) {
    table.remove(name);
  }

  @Override
  public String toString() {
    StringBuilder representation = new StringBuilder("ReferenceTable: {\n");
    for (Map.Entry<String, LocalReference> elt : table.entrySet()) {
      representation.append(elt.getKey()).append(": ").append(elt.getValue()).append('\n');
    }
    representation.append('}');
    if (parent != null && parent != this) {
      representation.append(" => ").append(parent.toString());
    }
    return representation.toString();
  }
}
