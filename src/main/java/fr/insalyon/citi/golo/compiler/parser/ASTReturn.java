package fr.insalyon.citi.golo.compiler.parser;

public class ASTReturn extends GoloASTNode {

  public ASTReturn(int id) {
    super(id);
  }

  public ASTReturn(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTReturn{}";
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
