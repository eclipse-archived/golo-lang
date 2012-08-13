package gololang.compiler.parser;

public class ASTAssociativeExpression extends GoloASTNode {

  private String operator;

  public ASTAssociativeExpression(int id) {
    super(id);
  }

  public ASTAssociativeExpression(GoloParser p, int id) {
    super(p, id);
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public String toString() {
    return "ASTAssociativeExpression{" +
        "operator='" + operator + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
