package gololang.compiler.parser;

public class ASTFunctionDeclaration extends SimpleNode {

  private String name;

  public ASTFunctionDeclaration(int i) {
    super(i);
  }

  public ASTFunctionDeclaration(GoloParser p, int i) {
    super(p, i);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ASTFunctionDeclaration{" +
        "name='" + name + '\'' +
        '}';
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
