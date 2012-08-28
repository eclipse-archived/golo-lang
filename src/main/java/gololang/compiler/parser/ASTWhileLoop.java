package gololang.compiler.parser;

public class ASTWhileLoop extends GoloASTNode {

  public ASTWhileLoop(int id) {
    super(id);
  }

  public ASTWhileLoop(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTWhileLoop{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
