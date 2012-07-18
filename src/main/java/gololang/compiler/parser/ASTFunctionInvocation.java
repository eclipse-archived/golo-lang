package gololang.compiler.parser;

public class ASTFunctionInvocation extends GoloASTNode {

  public ASTFunctionInvocation(int id) {
    super(id);
  }

  public ASTFunctionInvocation(GoloParser p, int id) {
    super(p, id);
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
