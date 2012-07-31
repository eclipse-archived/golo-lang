package gololang.compiler.ast;

public class AssignmentStatement extends GoloStatement {

  private final LocalReference localReference;
  private final ExpressionStatement expressionStatement;

  public AssignmentStatement(LocalReference localReference, ExpressionStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.localReference = localReference;
    this.expressionStatement = expressionStatement;
  }

  public LocalReference getLocalReference() {
    return localReference;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  @Override
  public void accept(GoloAstVisitor visitor) {

  }
}
