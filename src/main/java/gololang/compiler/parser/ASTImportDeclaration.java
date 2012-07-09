package gololang.compiler.parser;

public class ASTImportDeclaration extends SimpleNode {

  private String name;

  public ASTImportDeclaration(int i) {
    super(i);
  }

  public ASTImportDeclaration(GoloParser p, int i) {
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
    return "ASTImportDeclaration{" +
        "name='" + name + '\'' +
        '}';
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
