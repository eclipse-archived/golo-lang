package gololang.compiler.ast;

public abstract class ExpressionStatement extends GoloStatement {

  public ExpressionStatement(PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
  }
}
