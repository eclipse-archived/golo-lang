package gololang.compiler.parser;

public class ASTModuleDeclaration extends SimpleNode {

  private String name;

  public ASTModuleDeclaration(int i) {
    super(i);
  }

  public ASTModuleDeclaration(GoloParser p, int i) {
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
    return "ASTModuleDeclaration{" +
        "name='" + name + '\'' +
        '}';
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
