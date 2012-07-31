package gololang.compiler.ast;

public class ReferenceLookup extends ExpressionStatement {

  private final String name;

  public ReferenceLookup(String name, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public LocalReference resolveIn(ReferenceTable referenceTable) {
    return referenceTable.get(name);
  }

  @Override
  public void accept(GoloAstVisitor visitor) {
    visitor.visitReferenceLookup(this);
  }
}
