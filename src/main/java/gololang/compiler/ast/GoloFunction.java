package gololang.compiler.ast;

public final class GoloFunction {

  public static enum Visibility {
    PUBLIC, LOCAL
  }

  private final String name;
  private final Visibility visibility;
  private final int arity;
  private final boolean varargs;
  private final int lineInSourceCode;
  private final int columnInSourceCode;

  public GoloFunction(String name, Visibility visibility, int arity, boolean varargs, int lineInSourceCode, int columnInSourceCode) {
    this.name = name;
    this.visibility = visibility;
    this.arity = arity;
    this.varargs = varargs;
    this.lineInSourceCode = lineInSourceCode;
    this.columnInSourceCode = columnInSourceCode;
  }

  public String getName() {
    return name;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public int getArity() {
    return arity;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public int getLineInSourceCode() {
    return lineInSourceCode;
  }

  public int getColumnInSourceCode() {
    return columnInSourceCode;
  }
}
