package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.runtime.OperatorType;

public class BinaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private final ExpressionStatement leftExpression;
  private final ExpressionStatement rightExpression;

  public BinaryOperation(OperatorType type, ExpressionStatement leftExpression, ExpressionStatement rightExpression, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.type = type;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public OperatorType getType() {
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
