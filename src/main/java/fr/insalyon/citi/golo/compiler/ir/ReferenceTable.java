package fr.insalyon.citi.golo.compiler.ir;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

public final class ReferenceTable {

  private final ReferenceTable parent;
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

  public void remove(String name) {
    table.remove(name);
  }
}
