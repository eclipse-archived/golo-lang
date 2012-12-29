package fr.insalyon.citi.golo.compiler.ir;

public class FunctionInvocation extends AbstractInvocation {

  private boolean onReference = false;

  public FunctionInvocation(String name, PositionInSourceCode positionInSourceCode) {
    super(name, positionInSourceCode);
  }

  public boolean isOnReference() {
    return onReference;
  }

  public void setOnReference(boolean onReference) {
    this.onReference = onReference;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
