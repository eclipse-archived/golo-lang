package fr.insalyon.citi.golo.compiler.parser;

public class ASTPimpDeclaration extends GoloASTNode {

  private String target;

  public ASTPimpDeclaration(int id) {
    super(id);
  }

  public ASTPimpDeclaration(GoloParser p, int id) {
    super(p, id);
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "ASTPimpDeclaration{" +
        "target='" + target + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
