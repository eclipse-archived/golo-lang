package gololang.compiler.ast;

public class ConstantStatement extends ExpressionStatement {

  private final Object value;

  public ConstantStatement(Object value, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.value = value;
  }

  public Object getValue() {
    return value;
  }
}
