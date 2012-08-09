package gololang.compiler.ir;

public final class ConditionalBranching extends GoloStatement {

  private final ExpressionStatement condition;
  private final Block trueBlock;
  private final ConditionalBranching elseConditionalBranching;
  private final Block falseBlock;

  public ConditionalBranching(ExpressionStatement condition, Block trueBlock, Block falseBlock, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.condition = condition;
    this.trueBlock = trueBlock;
    this.falseBlock = falseBlock;
    this.elseConditionalBranching = null;
  }

  public ConditionalBranching(ExpressionStatement condition, Block trueBlock, ConditionalBranching elseConditionalBranching, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.condition = condition;
    this.trueBlock = trueBlock;
    this.elseConditionalBranching = elseConditionalBranching;
    this.falseBlock = null;
  }

  public ExpressionStatement getCondition() {
    return condition;
  }

  public Block getTrueBlock() {
    return trueBlock;
  }

  public Block getFalseBlock() {
    return falseBlock;
  }

  public boolean hasFalseBlock() {
    return falseBlock != null;
  }

  public ConditionalBranching getElseConditionalBranching() {
    return elseConditionalBranching;
  }

  public boolean hasElseConditionalBranching() {
    return elseConditionalBranching != null;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConditionalBranching(this);
  }
}
