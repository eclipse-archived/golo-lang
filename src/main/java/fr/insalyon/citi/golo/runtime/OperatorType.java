package fr.insalyon.citi.golo.runtime;

public enum OperatorType {

  PLUS("+"),
  MINUS("-"),
  TIMES("*"),
  DIVIDE("/"),

  EQUALS("=="),
  NOTEQUALS("!="),
  LESS("<"),
  LESSOREQUALS("<="),
  MORE(">"),
  MOREOREQUALS(">="),

  AND("and"),
  OR("or"),
  NOT("not"),

  IS("is"),
  ISNT("isnt"),

  OFTYPE("oftype"),

  METHOD_CALL(":")
  ;

  private final String symbol;

  OperatorType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
