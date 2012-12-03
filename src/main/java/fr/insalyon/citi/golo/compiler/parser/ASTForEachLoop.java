package fr.insalyon.citi.golo.compiler.parser;

public class ASTForEachLoop extends GoloASTNode {

  private String elementIdentifier;

  public ASTForEachLoop(int id) {
    super(id);
  }

  public ASTForEachLoop(GoloParser p, int id) {
    super(p, id);
  }

  public String getElementIdentifier() {
    return elementIdentifier;
  }

  public void setElementIdentifier(String elementIdentifier) {
    this.elementIdentifier = elementIdentifier;
  }

  @Override
  public String toString() {
    return "ASTForEachLoop{" +
        "elementIdentifier='" + elementIdentifier + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
