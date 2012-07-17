package gololang.compiler.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class GoloFunction {

  public static enum Visibility {
    PUBLIC, LOCAL
  }

  private final String name;
  private final Visibility visibility;
  private final int arity;
  private final boolean varargs;
  private final PositionInSourceCode positionInSourceCode;

  private GoloBlock block;

  public GoloFunction(String name, Visibility visibility, int arity, boolean varargs, PositionInSourceCode positionInSourceCode) {
    this.name = name;
    this.visibility = visibility;
    this.arity = arity;
    this.varargs = varargs;
    this.positionInSourceCode = positionInSourceCode;
  }

  public String getName() {
    return name;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public int getArity() {
    return arity;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public PositionInSourceCode getPositionInSourceCode() {
    return positionInSourceCode;
  }

  public GoloBlock getBlock() {
    return block;
  }

  public void setBlock(GoloBlock block) {
    this.block = block;
  }
}
