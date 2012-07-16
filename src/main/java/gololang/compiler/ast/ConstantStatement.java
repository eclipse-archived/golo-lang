package gololang.compiler.ast;

public class ConstantStatement extends ExpressionStatement {

  private final Object value;

  public ConstantStatement(Object value, int lineInSourceCode, int columnInSourceCode) {
    super(lineInSourceCode, columnInSourceCode);
    this.value = value;
  }

  public Object getValue() {
    return value;
  }
}
