/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

public final class PositionInSourceCode {

  private final int line;
  private final int column;

  public PositionInSourceCode(int line, int column) {
    this.line = line;
    this.column = column;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public boolean isNull() {
    return line == 0 && column == 0;
  }

  @Override
  public String toString() {
    return String.format("{line=%d, column=%d}", line, column);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    PositionInSourceCode that = (PositionInSourceCode) o;
    return column == that.column && line == that.line;
  }

  @Override
  public int hashCode() {
    int result = line;
    result = 31 * result + column;
    return result;
  }
}
