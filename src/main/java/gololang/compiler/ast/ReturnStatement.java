package gololang.compiler.ast;

public class ReturnStatement extends GoloStatement {

  private final GoloStatement expressionStatement;

  public ReturnStatement(ExpressionStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.expressionStatement = expressionStatement;
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }
}
