/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.utils;

public final class StringBlockIndenter {

  private StringBlockIndenter() {
    // utility class
  }

  public static String unindent(String block, int columns) {
    assert columns >= 0;
    String[] lines = block.split("\\n");
    StringBuilder result = new StringBuilder();
    for (String line : lines) {
      if (line.length() > columns) {
        result.append(line.substring(columns));
      } else {
        result.append(line);
      }
      result.append("\n");
    }
    return result.toString();
  }
}
