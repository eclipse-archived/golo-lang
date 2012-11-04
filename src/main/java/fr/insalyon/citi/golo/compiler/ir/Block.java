package fr.insalyon.citi.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class Block extends GoloStatement {

  private final List<GoloStatement> statements = new LinkedList<>();
  private final ReferenceTable referenceTable;

  private boolean hasReturn = false;

  public Block(ReferenceTable referenceTable) {
    super(new PositionInSourceCode(-1, -1));
    this.referenceTable = referenceTable;
  }

  public ReferenceTable getReferenceTable() {
    return referenceTable;
  }

  public List<GoloStatement> getStatements() {
    return unmodifiableList(statements);
  }

  public void addStatement(GoloStatement statement) {
    statements.add(statement);
    if ((statement instanceof ReturnStatement) || (statement instanceof ThrowStatement)) {
      hasReturn = true;
    } else if (statement instanceof ConditionalBranching) {
      hasReturn = hasReturn || ((ConditionalBranching) statement).returnsFromBothBranches();
    }
  }

  public boolean hasReturn() {
    return hasReturn;
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitBlock(this);
  }
}
