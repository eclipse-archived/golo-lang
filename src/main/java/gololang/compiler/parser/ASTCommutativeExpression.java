package gololang.compiler.parser;

public class ASTCommutativeExpression extends GoloASTNode {

  private String operator;

  public ASTCommutativeExpression(int id) {
    super(id);
  }

  public ASTCommutativeExpression(GoloParser p, int id) {
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
    return "ASTCommutativeExpression{" +
        "operator='" + operator + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
