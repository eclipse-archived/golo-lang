package gololang.compiler.parser;

public class ASTForLoop extends GoloASTNode {

  public ASTForLoop(int id) {
    super(id);
  }

  public ASTForLoop(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTForLoop{}";
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
