package gololang.compiler.parser;

public class ASTFunctionInvocation extends GoloASTNode {

  private String name;

  public ASTFunctionInvocation(int id) {
    super(id);
  }

  public ASTFunctionInvocation(GoloParser p, int id) {
    super(p, id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ASTFunctionInvocation{" +
        "name='" + name + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
