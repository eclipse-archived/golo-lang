package gololang.compiler.ast;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class Block {

  private final List<GoloStatement> statements = new LinkedList<>();

  public List<GoloStatement> getStatements() {
    return unmodifiableList(statements);
  }

  public void addStatement(GoloStatement statement) {
    statements.add(statement);
  }

  public void accept(GoloAstVisitor visitor) {
    visitor.visitBlock(this);
  }
}
