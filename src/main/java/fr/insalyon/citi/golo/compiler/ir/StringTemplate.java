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

import java.util.LinkedList;
import java.util.List;

public class StringTemplate extends ExpressionStatement {

  private List<Object> parts;

  public StringTemplate(List<Object> parts) {
    this.parts = parts;
  }

  public List<Object> getParts() {
    return parts;
  }

  public List<ExpressionStatement> getExpressions() {
    List<ExpressionStatement> expressions = new LinkedList<>();
    for(Object part : parts) {
      if(part instanceof ExpressionStatement) {
        expressions.add((ExpressionStatement) part);
      }
    }
    return expressions;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitStringTemplate(this);
  }
}
