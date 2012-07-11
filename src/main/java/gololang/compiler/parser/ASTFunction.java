package gololang.compiler.parser;

import java.util.List;

public class ASTFunction extends GoloASTNode {

  private List<String> arguments;

  public ASTFunction(int i) {
    super(i);
  }

  public ASTFunction(GoloParser p, int i) {
    super(p, i);
  }

  public List<String> getArguments() {
    return arguments;
  }

  public void setArguments(List<String> arguments) {
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    return "ASTFunction{" +
        "arguments=" + arguments +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
