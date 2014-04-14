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

import fr.insalyon.citi.golo.runtime.OperatorType;

public class BinaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private final ExpressionStatement leftExpression;
  private final ExpressionStatement rightExpression;

  public BinaryOperation(OperatorType type, ExpressionStatement leftExpression, ExpressionStatement rightExpression) {
    super();
    this.type = type;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public OperatorType getType() {
    return type;
  }

  public ExpressionStatement getLeftExpression() {
    return leftExpression;
  }

  public ExpressionStatement getRightExpression() {
    return rightExpression;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBinaryOperation(this);
  }
}
