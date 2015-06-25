/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime;

public enum OperatorType {

  PLUS("+"),
  MINUS("-"),
  TIMES("*"),
  DIVIDE("/"),
  MODULO("%"),

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

  ORIFNULL("orIfNull"),

  ANON_CALL(""),
  METHOD_CALL(":"),
  ELVIS_METHOD_CALL("?:");

  private final String symbol;

  OperatorType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
