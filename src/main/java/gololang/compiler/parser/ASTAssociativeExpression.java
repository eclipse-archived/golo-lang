package gololang.compiler.parser;

public class ASTAssociativeExpression extends GoloASTNode {

  public ASTAssociativeExpression(int id) {
    super(id);
  }

  public ASTAssociativeExpression(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
