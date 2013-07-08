/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import java.util.List;

public class CollectionLiteral extends ExpressionStatement {

  public static enum Type {
    array, list, set, map, tuple, vector
  }

  private final Type type;
  private final List<ExpressionStatement> expressions;

  public CollectionLiteral(Type type, List<ExpressionStatement> expressions) {
    this.type = type;
    this.expressions = expressions;
  }

  public Type getType() {
    return type;
  }

  public List<ExpressionStatement> getExpressions() {
    return expressions;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.acceptCollectionLiteral(this);
  }
}
