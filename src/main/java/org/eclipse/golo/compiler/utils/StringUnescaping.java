/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.utils;

public final class StringUnescaping {

  private StringUnescaping() {
    //utility class
  }

  public static String unescape(String str) {
    StringBuilder sb = new StringBuilder(str.length());
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch == '\\') {
        char nextChar = (i == str.length() - 1) ? '\\' : str.charAt(i + 1);
        switch (nextChar) {
          case '\\':
            ch = '\\';
            break;
          case 'b':
            ch = '\b';
            break;
          case 'f':
            ch = '\f';
            break;
          case 'n':
            ch = '\n';
            break;
          case 'r':
            ch = '\r';
            break;
          case 't':
            ch = '\t';
            break;
          case '\"':
            ch = '\"';
            break;
          case '\'':
            ch = '\'';
            break;
          default:
            // not a special char, do nothing
            break;
        }
        i++;
      }
      sb.append(ch);
    }
    return sb.toString();
  }
}
