package fr.insalyon.citi.golo.compiler.parser;

public class ASTConditionalBranching extends GoloASTNode {

  public ASTConditionalBranching(int id) {
    super(id);
  }

  public ASTConditionalBranching(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTConditionalBranching{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
