package fr.insalyon.citi.golo.compiler.parser;

public class ASTFunctionDeclaration extends GoloASTNode {

  private String name;
  private boolean local = false;
  private boolean pimp = false;

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

  public boolean isLocal() {
    return local;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public boolean isPimp() {
    return pimp;
  }

  public void setPimp(boolean pimp) {
    this.pimp = pimp;
  }

  @Override
  public String toString() {
    return "ASTFunctionDeclaration{" +
        "name='" + name + '\'' +
        ", local=" + local +
        ", pimp=" + pimp +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
