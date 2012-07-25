package gololang.compiler.ast;

import java.util.*;

public final class ReferenceTable {

  private final Map<String, LocalReference> table = new HashMap<>();

  public ReferenceTable add(LocalReference reference) {
    table.put(reference.getName(), reference);
    return this;
  }

  public boolean hasReferenceFor(String name) {
    return table.containsKey(name);
  }

  public LocalReference get(String name) {
    return table.get(name);
  }

  public Set<String> symbols() {
    return Collections.unmodifiableSet(table.keySet());
  }

  public Collection<LocalReference> references() {
    return Collections.unmodifiableCollection(table.values());
  }

  public ReferenceTable fork() {
    ReferenceTable fork = new ReferenceTable();
    fork.table.putAll(table);
    return fork;
  }
}
