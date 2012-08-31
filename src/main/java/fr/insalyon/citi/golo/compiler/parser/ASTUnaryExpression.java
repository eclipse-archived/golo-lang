package fr.insalyon.citi.golo.compiler.parser;

public class ASTUnaryExpression extends GoloASTNode {

  private String operator;

  public ASTUnaryExpression(int id) {
    super(id);
  }

  public ASTUnaryExpression(GoloParser p, int id) {
    super(p, id);
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
