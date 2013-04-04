package fr.insalyon.citi.golo.compiler.ir;

public class LoopBreakFlowStatement extends GoloStatement {

  public static enum Type {
    BREAK, CONTINUE
  }

  private final Type type;

  private LoopBreakFlowStatement(Type type) {
    super();
    this.type = type;
  }

  public static LoopBreakFlowStatement newContinue() {
    return new LoopBreakFlowStatement(Type.CONTINUE);
  }

  public static LoopBreakFlowStatement newBreak() {
    return new LoopBreakFlowStatement(Type.BREAK);
  }

  public Type getType() {
    return type;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptLoopBreakFlowStatement(this);
  }
}
