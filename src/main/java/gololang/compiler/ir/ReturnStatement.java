package gololang.compiler.ir;

public class ReturnStatement extends GoloStatement {

  private final GoloStatement expressionStatement;

  public ReturnStatement(ExpressionStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.expressionStatement = expressionStatement;
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReturnStatement(this);
  }
}
