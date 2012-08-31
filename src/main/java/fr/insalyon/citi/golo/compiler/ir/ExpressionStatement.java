package fr.insalyon.citi.golo.compiler.ir;

public abstract class ExpressionStatement extends GoloStatement {

  public ExpressionStatement(PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
  }
}
