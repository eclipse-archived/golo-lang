package gololang.compiler.parser;

public class ASTLiteral extends SimpleNode {

  private Object literalValue;

  public ASTLiteral(int i) {
    super(i);
  }

  public ASTLiteral(GoloParser p, int i) {
    super(p, i);
  }

  public Object getLiteralValue() {
    return literalValue;
  }

  public void setLiteralValue(Object literalValue) {
    this.literalValue = literalValue;
  }

  @Override
  public String toString() {
    return "ASTLiteral{" +
        "literalValue=" + literalValue +
        '}';
  }

  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
