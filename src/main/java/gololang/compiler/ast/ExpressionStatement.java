package gololang.compiler.ast;

public abstract class ExpressionStatement extends GoloStatement {

  public ExpressionStatement(int lineInSourceCode, int columnInSourceCode) {
    super(lineInSourceCode, columnInSourceCode);
  }
}
