package fr.insalyon.citi.golo.compiler.parser;

public class ASTLetOrVar extends GoloASTNode {

  public static enum Type {
    LET, VAR
  }

  private Type type;
  private String name;

  public ASTLetOrVar(int id) {
    super(id);
  }

  public ASTLetOrVar(GoloParser p, int id) {
    super(p, id);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ASTLetOrVar{" +
        "type=" + type +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
