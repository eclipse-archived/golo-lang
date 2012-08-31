package fr.insalyon.citi.golo.compiler.parser;

public class GoloASTNode extends SimpleNode {

  private int lineInSourceCode = -1;
  private int columnInSourceCode = -1;

  public GoloASTNode(int i) {
    super(i);
  }

  public GoloASTNode(GoloParser p, int i) {
    super(p, i);
  }

  public int getLineInSourceCode() {
    return lineInSourceCode;
  }

  public void setLineInSourceCode(int lineInSourceCode) {
    this.lineInSourceCode = lineInSourceCode;
  }

  public int getColumnInSourceCode() {
    return columnInSourceCode;
  }

  public void setColumnInSourceCode(int columnInSourceCode) {
    this.columnInSourceCode = columnInSourceCode;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
