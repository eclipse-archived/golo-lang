package fr.insalyon.citi.golo.compiler.ir;

public class MethodInvocation extends AbstractInvocation {

  private final ExpressionStatement target;

  public MethodInvocation(ExpressionStatement target, String name, PositionInSourceCode positionInSourceCode) {
    super(name, positionInSourceCode);
    this.target = target;
  }

  public ExpressionStatement getTarget() {
    return target;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptMethodInvocation(this);
  }
}
