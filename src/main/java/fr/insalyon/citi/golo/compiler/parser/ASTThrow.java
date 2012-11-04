package fr.insalyon.citi.golo.compiler.parser;

public class ASTThrow extends GoloASTNode {

  public ASTThrow(int id) {
    super(id);
  }

  public ASTThrow(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return "ASTThrow{}";
  }
}

