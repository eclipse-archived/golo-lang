package fr.insalyon.citi.golo.compiler.ir;

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
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReferenceLookup(this);
  }
}
