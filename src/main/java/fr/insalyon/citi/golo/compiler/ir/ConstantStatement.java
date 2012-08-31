package fr.insalyon.citi.golo.compiler.ir;

public class ConstantStatement extends ExpressionStatement {

  private final Object value;

  public ConstantStatement(Object value, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConstantStatement(this);
  }
}
