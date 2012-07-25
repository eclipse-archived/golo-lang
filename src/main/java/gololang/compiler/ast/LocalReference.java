package gololang.compiler.ast;

public final class LocalReference {

  public static enum Kind {
    CONSTANT, VARIABLE
  }

  private final Kind kind;
  private final String name;

  public LocalReference(Kind kind, String name) {
    this.kind = kind;
    this.name = name;
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "LocalReference{" +
        "kind=" + kind +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LocalReference that = (LocalReference) o;

    if (kind != that.kind) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = kind.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
