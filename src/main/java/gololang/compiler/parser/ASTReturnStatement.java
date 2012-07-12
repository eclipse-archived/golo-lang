package gololang.compiler.parser;

public class ASTReturnStatement extends GoloASTNode {

  public ASTReturnStatement(int id) {
    super(id);
  }

  public ASTReturnStatement(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return "ASTReturnStatement{}";
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
