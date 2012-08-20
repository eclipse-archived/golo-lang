package gololang.compiler.ir;

import gololang.runtime.OperatorType;

public class UnaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private final ExpressionStatement expressionStatement;

  public UnaryOperation(OperatorType type, ExpressionStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.type = type;
    this.expressionStatement = expressionStatement;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  public OperatorType getType() {
    return type;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnaryOperation(this);
  }
}
