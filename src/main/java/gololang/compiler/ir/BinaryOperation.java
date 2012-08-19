package gololang.compiler.ir;

public class BinaryOperation extends ExpressionStatement {

  public static enum Type {

    PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/");

    private final String symbol;

    private Type(String symbol) {
      this.symbol = symbol;
    }

    @Override
    public String toString() {
      return symbol;
    }
  }

  private final Type type;
  private final ExpressionStatement leftExpression;
  private final ExpressionStatement rightExpression;

  public BinaryOperation(Type type, ExpressionStatement leftExpression, ExpressionStatement rightExpression, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.type = type;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public Type getType() {
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
