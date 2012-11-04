package fr.insalyon.citi.golo.compiler.ir;

public class ThrowStatement extends GoloStatement {

  private final GoloStatement expressionStatement;

  public ThrowStatement(GoloStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.expressionStatement = expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitThrowStatement(this);
  }

  public GoloStatement getExpressionStatement() {
    return expressionStatement;
  }
}
