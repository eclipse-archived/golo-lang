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

package fr.insalyon.citi.golo.compiler.parser;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ASTAssociativeExpression extends GoloASTNode {

  private final List<String> operators = new LinkedList<>();
  private final List<Integer> lines = new LinkedList<>();
  private final List<Integer> columns = new LinkedList<>();

  public ASTAssociativeExpression(int id) {
    super(id);
  }

  public ASTAssociativeExpression(GoloParser p, int id) {
    super(p, id);
  }

  public void addOperator(String symbol) {
    operators.add(symbol);
  }

  public List<String> getOperators() {
    return unmodifiableList(operators);
  }

  public void addLine(int line) {
    lines.add(line);
  }

  public void addColumn(int column) {
    columns.add(column);
  }

  public List<Integer> getLines() {
    return unmodifiableList(lines);
  }

  public List<Integer> getColumns() {
    return unmodifiableList(columns);
  }

  @Override
  public String toString() {
    return "ASTAssociativeExpression{" +
        "operators=" + operators +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
