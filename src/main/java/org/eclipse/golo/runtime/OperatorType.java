/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.util.HashMap;

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

  private static final HashMap<String, OperatorType> SYMBOL_MAPPING = new HashMap<>();
  static {
    for (OperatorType op : values()) {
      SYMBOL_MAPPING.put(op.toString(), op);
    }
  }

  OperatorType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }

  public static OperatorType fromString(String symbol) {
    if (!SYMBOL_MAPPING.containsKey(symbol)) {
      throw new IllegalArgumentException("Unknown symbol for OperatorType: " + symbol);
    }
    return SYMBOL_MAPPING.get(symbol);
  }
}
