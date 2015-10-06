/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.utils;

public class StringBlockIndenter {

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
