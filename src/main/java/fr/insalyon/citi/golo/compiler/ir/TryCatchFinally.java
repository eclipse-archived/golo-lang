package fr.insalyon.citi.golo.compiler.ir;

public class TryCatchFinally extends GoloStatement {

  private final String exceptionId;
  private final Block tryBlock;
  private final Block catchBlock;
  private final Block finallyBlock;
  private final boolean tryFinally;

  public TryCatchFinally(String exceptionId, Block tryBlock, Block catchBlock, Block finallyBlock, boolean tryFinally, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.exceptionId = exceptionId;
    this.tryBlock = tryBlock;
    this.catchBlock = catchBlock;
    this.finallyBlock = finallyBlock;
    this.tryFinally = tryFinally;
  }

  public String getExceptionId() {
    return exceptionId;
  }

  public Block getTryBlock() {
    return tryBlock;
  }

  public Block getCatchBlock() {
    return catchBlock;
  }

  public Block getFinallyBlock() {
    return finallyBlock;
  }

  public boolean hasFinallyBlock() {
    return finallyBlock != null;
  }

  public boolean isTryFinally() {
    return tryFinally;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitTryCatchFinally(this);
  }
}
