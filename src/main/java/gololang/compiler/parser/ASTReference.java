package gololang.compiler.parser;

public class ASTReference extends GoloASTNode {

  private String name;

  public ASTReference(int id) {
    super(id);
  }

  public ASTReference(GoloParser p, int id) {
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
    return "ASTReference{" +
        "name='" + name + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
