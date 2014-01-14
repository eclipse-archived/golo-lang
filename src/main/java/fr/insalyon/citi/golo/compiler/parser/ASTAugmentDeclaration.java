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

public class ASTAugmentDeclaration extends GoloASTNode implements NamedNode {

  private String target;

  public ASTAugmentDeclaration(int id) {
    super(id);
  }

  public ASTAugmentDeclaration(GoloParser p, int id) {
    super(p, id);
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "ASTAugmentDeclaration{" +
        "target='" + target + '\'' +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getName() {
    return getTarget();
  }

  @Override
  public void setName(String name) {
    setTarget(name);
  }
}
