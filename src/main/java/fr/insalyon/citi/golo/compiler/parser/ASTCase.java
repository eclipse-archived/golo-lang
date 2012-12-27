package fr.insalyon.citi.golo.compiler.parser;

public class ASTCase extends GoloASTNode {

  public ASTCase(int id) {
    super(id);
  }

  public ASTCase(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return "ASTBlock{}";
  }
}
