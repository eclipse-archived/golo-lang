package gololang.compiler.ir;

import gololang.runtime.BinaryOperationType;

public class BinaryOperation extends ExpressionStatement {

  private final BinaryOperationType type;
  private final ExpressionStatement leftExpression;
  private final ExpressionStatement rightExpression;

  public BinaryOperation(BinaryOperationType type, ExpressionStatement leftExpression, ExpressionStatement rightExpression, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.type = type;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public BinaryOperationType getType() {
    return type;
  }

  public ExpressionStatement getLeftExpression() {
    return leftExpression;
  }

  public ExpressionStatement getRightExpression() {
    return rightExpression;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptBinaryOperation(this);
  }
}
