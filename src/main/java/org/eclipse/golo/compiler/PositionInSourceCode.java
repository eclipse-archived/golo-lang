/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

public class PositionInSourceCode {

  private final int startLine;
  private final int startColumn;
  private final int endLine;
  private final int endColumn;

  private static final PositionInSourceCode UNDEFINED = new PositionInSourceCode(0, 0, 0, 0) {
    @Override
    public boolean isUndefined() {
      return true;
    }

    @Override
    public String toString() {
      return "undefined";
    }
  };

  private PositionInSourceCode(int startLine, int startColumn, int endLine, int endColumn) {
    this.startLine = startLine;
    this.endLine = endLine;
    this.startColumn = startColumn;
    this.endColumn = endColumn;
  }

  public static PositionInSourceCode undefined() {
    return UNDEFINED;
  }

  public static PositionInSourceCode of(PositionInSourceCode pos) {
    if (pos == null) {
      return UNDEFINED;
    }
    return pos;
  }

  public static PositionInSourceCode of(int line, int column) {
    return of(line, column, line, column);
  }

  public static PositionInSourceCode of(int startLine, int startColumn, int endLine, int endColumn) {
    if (startLine <= 0 && endLine <= 0 && startColumn <= 0 && endColumn <= 0) {
      return UNDEFINED;
    }
    return new PositionInSourceCode(startLine, startColumn, endLine, endColumn);
  }

  public int getStartLine() {
    return startLine;
  }

  public int getStartColumn() {
    return startColumn;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndColumn() {
    return endColumn;
  }

  public boolean isUndefined() {
    return false;
  }

  @Override
  public String toString() {
    if (startLine == endLine && startColumn == endColumn) {
      return String.format("{line=%d, column=%d}", startLine, startColumn);
    }
    return String.format("{from=%d;%d, to=%d;%d}", startLine, startColumn, endLine, endColumn);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    PositionInSourceCode that = (PositionInSourceCode) o;
    return startColumn == that.startColumn
      && startLine == that.startLine
      && endLine == that.endLine
      && endColumn == that.endColumn;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(startLine, startColumn, endLine, endColumn);
  }
}
