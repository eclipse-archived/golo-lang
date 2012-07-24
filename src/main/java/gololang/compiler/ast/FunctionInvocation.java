package gololang.compiler.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FunctionInvocation extends ExpressionStatement {

  private final String name;
  private final List<ExpressionStatement> arguments = new LinkedList<>();

  public FunctionInvocation(String name, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addArgument(ExpressionStatement argument) {
    arguments.add(argument);
  }

  public List<ExpressionStatement> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public int getArity() {
    return arguments.size();
  }

  @Override
  public void accept(GoloAstVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
