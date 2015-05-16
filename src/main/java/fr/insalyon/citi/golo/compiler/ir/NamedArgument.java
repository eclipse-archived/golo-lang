/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

public class NamedArgument extends ExpressionStatement {

  private String name;
  private ExpressionStatement expression;

  public NamedArgument(String name, ExpressionStatement expression) {
    this.name = name;
    this.expression = expression;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ExpressionStatement getExpression() {
    return expression;
  }

  public void setExpression(ExpressionStatement expression) {
    this.expression = expression;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {

  }
}
