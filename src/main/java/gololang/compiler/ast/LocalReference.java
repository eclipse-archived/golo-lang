package gololang.compiler.ast;

public final class LocalReference {

  public static enum Kind {
    CONSTANT, VARIABLE
  }

  private final Kind kind;
  private final String name;

  private int index = -1;

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

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "LocalReference{" +
        "kind=" + kind +
        ", name='" + name + '\'' +
        ", index=" + index +
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
