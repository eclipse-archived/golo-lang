package gololang.compiler.parser;

public class ASTCommutativeExpression extends GoloASTNode {

  public ASTCommutativeExpression(int id) {
    super(id);
  }

  public ASTCommutativeExpression(GoloParser p, int id) {
    super(p, id);
  }

   @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
