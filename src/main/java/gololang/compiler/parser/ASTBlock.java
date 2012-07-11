package gololang.compiler.parser;

public class ASTBlock extends SimpleNode {

  public ASTBlock(int id) {
    super(id);
  }

  public ASTBlock(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTBlock{}";
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
