package fr.insalyon.citi.golo.compiler.parser;

public class ASTBreak extends GoloASTNode {

  public ASTBreak(int id) {
    super(id);
  }

  public ASTBreak(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTBreak{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
