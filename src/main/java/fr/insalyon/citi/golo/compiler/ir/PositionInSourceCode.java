/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler.ir;

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

  @Override
  public String toString() {
    return "{" +
        "line=" + line +
        ", column=" + column +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PositionInSourceCode that = (PositionInSourceCode) o;

    if (column != that.column) return false;
    if (line != that.line) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = line;
    result = 31 * result + column;
    return result;
  }
}
