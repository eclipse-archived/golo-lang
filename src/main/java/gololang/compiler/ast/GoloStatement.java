package gololang.compiler.ast;

public abstract class GoloStatement {

  private final int lineInSourceCode;
  private final int columnInSourceCode;

  public GoloStatement(int lineInSourceCode, int columnInSourceCode) {
    this.lineInSourceCode = lineInSourceCode;
    this.columnInSourceCode = columnInSourceCode;
  }

  public int getLineInSourceCode() {
    return lineInSourceCode;
  }

  public int getColumnInSourceCode() {
    return columnInSourceCode;
  }
}
