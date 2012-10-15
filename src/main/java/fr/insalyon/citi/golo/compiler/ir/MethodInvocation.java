package fr.insalyon.citi.golo.compiler.ir;

public class MethodInvocation extends AbstractInvocation {

  public MethodInvocation(String name, PositionInSourceCode positionInSourceCode) {
    super(name, positionInSourceCode);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptMethodInvocation(this);
  }
}
