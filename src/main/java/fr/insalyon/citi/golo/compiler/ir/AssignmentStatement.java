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

public class AssignmentStatement extends GoloStatement {

  private LocalReference localReference;
  private final ExpressionStatement expressionStatement;
  private boolean declaring = false;

  public AssignmentStatement(LocalReference localReference, ExpressionStatement expressionStatement) {
    super();
    this.localReference = localReference;
    this.expressionStatement = expressionStatement;
  }

  public boolean isDeclaring() {
    return declaring;
  }

  public void setDeclaring(boolean declaring) {
    this.declaring = declaring;
  }

  public LocalReference getLocalReference() {
    return localReference;
  }

  public void setLocalReference(LocalReference localReference) {
    this.localReference = localReference;
  }

  public ExpressionStatement getExpressionStatement() {
    return expressionStatement;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitAssignmentStatement(this);
  }
}
