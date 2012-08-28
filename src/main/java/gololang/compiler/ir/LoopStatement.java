package gololang.compiler.ir;

public class LoopStatement extends GoloStatement {

  private final GoloStatement initStatement;
  private final ExpressionStatement conditionStatement;
  private final Block block;

  public LoopStatement(GoloStatement initStatement, ExpressionStatement conditionStatement, Block block, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.initStatement = initStatement;
    this.conditionStatement = conditionStatement;
    this.block = block;
  }

  public GoloStatement getInitStatement() {
    return initStatement;
  }

  public ExpressionStatement getConditionStatement() {
    return conditionStatement;
  }

  public Block getBlock() {
    return block;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }
}
