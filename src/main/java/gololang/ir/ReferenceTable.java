/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

public final class ReferenceTable {

  private ReferenceTable parent;
  private final Map<String, LocalReference> table = new LinkedHashMap<>();

  public ReferenceTable() {
    this(null);
  }

  private ReferenceTable(ReferenceTable parent) {
    this.parent = parent;
  }

  public ReferenceTable parent() {
    return this.parent;
  }

  public boolean isEmpty() {
    return this.table.isEmpty();
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

  public void updateFrom(GoloStatement<?> statement) {
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

  /**
   * Redefines the parent table for this table.
   *
   * <p>If {@code prune} is {@code true}, the owned references already presents in the parent are removed.
   *
   * @param parent the new parent table
   * @param prune remove duplicated owned references
   */
  public void relink(ReferenceTable parent, boolean prune) {
    if (parent == this) { return; }
    if (prune) {
      for (LocalReference reference : parent.references()) {
        if (this.table.containsKey(reference.getName())) {
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
    return this == other
      || this.parent == other
      || (this.parent != null && this.parent.isLinkedTo(other));
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

  /**
   * Return the set of the references known to this table.
   * <p>
   * Contains the own references as well as the ones from the parent table.
   */
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
    for (LocalReference reference : references()) {
      String refName = reference.getName();
      if (reference.isModuleState()) {
        referenceTable.add(LocalReference.of(refName)
            .kind(reference.getKind()));
        continue;
      }
      if (turnIntoConstants && !table.containsKey(refName)) {
        referenceTable.add(LocalReference.of(refName)
            .synthetic(reference.isSynthetic()));
      } else {
        referenceTable.add(LocalReference.of(refName)
            .synthetic(reference.isSynthetic())
            .kind(reference.getKind()));
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
