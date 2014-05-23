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

public class ASTLetOrVar extends GoloASTNode implements NamedNode {

  public static enum Type {
    LET, VAR
  }

  private Type type;
  private String name;
  private boolean moduleState = false;

  public ASTLetOrVar(int id) {
    super(id);
  }

  public ASTLetOrVar(GoloParser p, int id) {
    super(p, id);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public boolean isModuleState() {
    return moduleState;
  }

  public void setModuleState(boolean moduleState) {
    this.moduleState = moduleState;
  }

  @Override
  public String toString() {
    return "ASTLetOrVar{" +
        "type=" + type +
        ", name='" + name + '\'' +
        ", moduleState=" + moduleState +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
