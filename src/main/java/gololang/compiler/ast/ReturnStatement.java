package gololang.compiler.ast;

public class ReturnStatement extends GoloStatement {

  private final GoloStatement expressionStatement;

  public ReturnStatement(ExpressionStatement expressionStatement, int lineInSourceCode, int columnInSourceCode) {
    super(lineInSourceCode, columnInSourceCode);
    this.expressionStatement = expressionStatement;
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }
}
