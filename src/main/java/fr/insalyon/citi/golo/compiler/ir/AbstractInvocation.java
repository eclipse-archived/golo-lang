package fr.insalyon.citi.golo.compiler.ir;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractInvocation extends ExpressionStatement {

  private final String name;
  private final List<ExpressionStatement> arguments = new LinkedList<>();

  public AbstractInvocation(String name, PositionInSourceCode positionInSourceCode) {
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
}
