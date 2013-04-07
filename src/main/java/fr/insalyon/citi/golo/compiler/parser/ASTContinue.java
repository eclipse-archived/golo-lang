package fr.insalyon.citi.golo.compiler.parser;

public class ASTContinue extends GoloASTNode {

  public ASTContinue(int id) {
    super(id);
  }

  public ASTContinue(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTContinue{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
