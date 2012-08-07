package gololang.compiler.ir;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class GoloFunction {

  public static enum Visibility {
    PUBLIC, LOCAL
  }

  private final String name;
  private final Visibility visibility;
  private final PositionInSourceCode positionInSourceCode;

  private List<String> parameterNames = new LinkedList<>();
  private boolean varargs;
  private Block block;

  public GoloFunction(String name, Visibility visibility, PositionInSourceCode positionInSourceCode) {
    this.name = name;
    this.visibility = visibility;
    this.positionInSourceCode = positionInSourceCode;
  }

  public List<String> getParameterNames() {
    return unmodifiableList(parameterNames);
  }

  public void setParameterNames(List<String> parameterNames) {
    this.parameterNames.addAll(parameterNames);
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  public String getName() {
    return name;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public int getArity() {
    return parameterNames.size();
  }

  public boolean isVarargs() {
    return varargs;
  }

  public PositionInSourceCode getPositionInSourceCode() {
    return positionInSourceCode;
  }

  public Block getBlock() {
    return block;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunction(this);
  }
}
