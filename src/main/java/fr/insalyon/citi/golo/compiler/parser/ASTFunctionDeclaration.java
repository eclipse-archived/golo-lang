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

package fr.insalyon.citi.golo.compiler.parser;

public class ASTFunctionDeclaration extends GoloASTNode {

  private String name;
  private boolean local = false;
  private boolean pimp = false;

  public ASTFunctionDeclaration(int i) {
    super(i);
  }

  public ASTFunctionDeclaration(GoloParser p, int i) {
    super(p, i);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isLocal() {
    return local;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public boolean isPimp() {
    return pimp;
  }

  public void setPimp(boolean pimp) {
    this.pimp = pimp;
  }

  @Override
  public String toString() {
    return "ASTFunctionDeclaration{" +
        "name='" + name + '\'' +
        ", local=" + local +
        ", pimp=" + pimp +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
