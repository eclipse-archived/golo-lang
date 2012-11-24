package fr.insalyon.citi.golo.compiler.parser;

public class ASTTryCatchFinally extends GoloASTNode {

  private String exceptionId;

  public ASTTryCatchFinally(int id) {
    super(id);
  }

  public ASTTryCatchFinally(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public String getExceptionId() {
    return exceptionId;
  }

  public void setExceptionId(String exceptionId) {
    this.exceptionId = exceptionId;
  }

  @Override
  public String toString() {
    return "ASTTryCatchFinally{" +
        "exceptionId='" + exceptionId + '\'' +
        '}';
  }
}
