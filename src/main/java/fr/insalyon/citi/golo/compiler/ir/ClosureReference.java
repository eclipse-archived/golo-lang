package fr.insalyon.citi.golo.compiler.ir;

import java.util.HashSet;
import java.util.Set;

public class ClosureReference extends ExpressionStatement {

  private final GoloFunction target;
  private final Set<String> capturedReferenceNames = new HashSet<>();

  public ClosureReference(GoloFunction target, PositionInSourceCode positionInSourceCode) {
    super(positionInSourceCode);
    this.target = target;
  }

  public GoloFunction getTarget() {
    return target;
  }

  public Set<String> getCapturedReferenceNames() {
    return capturedReferenceNames;
  }

  public boolean addCapturedReferenceName(String s) {
    return capturedReferenceNames.add(s);
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptClosureReference(this);
  }
}
