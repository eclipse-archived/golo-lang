package fr.insalyon.citi.golo.compiler.ir;

public class AssignmentStatement extends GoloStatement {

  private LocalReference localReference;
  private final ExpressionStatement expressionStatement;

  public AssignmentStatement(LocalReference localReference, ExpressionStatement expressionStatement, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.localReference = localReference;
    this.expressionStatement = expressionStatement;
  }

  public LocalReference getLocalReference() {
    return localReference;
  }

  public void setLocalReference(LocalReference localReference) {
    this.localReference = localReference;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }
}
