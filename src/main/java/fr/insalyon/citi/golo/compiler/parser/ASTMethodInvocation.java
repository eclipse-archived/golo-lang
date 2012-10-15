package fr.insalyon.citi.golo.compiler.parser;

public class ASTMethodInvocation extends GoloASTNode {

  private String name;

  public ASTMethodInvocation(int id) {
    super(id);
  }

  public ASTMethodInvocation(GoloParser p, int id) {
    super(p, id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return "ASTMethodInvocation{" +
        "name='" + name + '\'' +
        '}';
  }
}
