package fr.insalyon.citi.golo.compiler.parser;

public class ASTAssignment extends GoloASTNode {

  private String name;

  public ASTAssignment(int id) {
    super(id);
  }

  public ASTAssignment(GoloParser p, int id) {
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
    return "ASTAssignment{" +
        "name='" + name + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
