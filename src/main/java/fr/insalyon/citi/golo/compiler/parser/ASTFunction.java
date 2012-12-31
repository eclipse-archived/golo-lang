package fr.insalyon.citi.golo.compiler.parser;

import java.util.List;

public class ASTFunction extends GoloASTNode {

  private List<String> arguments;
  private boolean varargs = false;
  private boolean compactForm = false;

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

  public boolean isVarargs() {
    return varargs;
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  public boolean isCompactForm() {
    return compactForm;
  }

  public void setCompactForm(boolean compactForm) {
    this.compactForm = compactForm;
  }

  @Override
  public String toString() {
    return "ASTFunction{" +
        "arguments=" + arguments +
        ", varargs=" + varargs +
        ", compactForm=" + compactForm +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
