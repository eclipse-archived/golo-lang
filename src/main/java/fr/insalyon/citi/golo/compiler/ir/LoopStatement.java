package fr.insalyon.citi.golo.compiler.ir;

public class LoopStatement extends GoloStatement {

  private final AssignmentStatement initStatement;
  private final ExpressionStatement conditionStatement;
  private final GoloStatement postStatement;
  private final Block block;

  public LoopStatement(AssignmentStatement initStatement, ExpressionStatement conditionStatement, Block block, GoloStatement postStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.initStatement = initStatement;
    this.conditionStatement = conditionStatement;
    this.postStatement = postStatement;
    this.block = block;
  }

  public boolean hasInitStatement() {
    return initStatement != null;
  }

  public AssignmentStatement getInitStatement() {
    return initStatement;
  }

  public ExpressionStatement getConditionStatement() {
    return conditionStatement;
  }

  public Block getBlock() {
    return block;
  }

  public GoloStatement getPostStatement() {
    return postStatement;
  }

  public boolean hasPostStatement() {
    return postStatement != null;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }
}
