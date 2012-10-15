package fr.insalyon.citi.golo.compiler.ir;

public class FunctionInvocation extends AbstractInvocation {

  public FunctionInvocation(String name, PositionInSourceCode positionInSourceCode) {
    super(name, positionInSourceCode);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
