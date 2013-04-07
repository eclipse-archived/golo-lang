package fr.insalyon.citi.golo.compiler.ir;

public class LoopBreakFlowStatement extends GoloStatement {

  public static enum Type {
    BREAK, CONTINUE
  }

  private final Type type;
  private LoopStatement enclosingLoop;

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

  public LoopStatement getEnclosingLoop() {
    return enclosingLoop;
  }

  public void setEnclosingLoop(LoopStatement enclosingLoop) {
    this.enclosingLoop = enclosingLoop;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptLoopBreakFlowStatement(this);
  }
}
