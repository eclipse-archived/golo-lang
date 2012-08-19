package gololang.runtime;

public enum BinaryOperationType {

  PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/");

  private final String symbol;

  BinaryOperationType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
