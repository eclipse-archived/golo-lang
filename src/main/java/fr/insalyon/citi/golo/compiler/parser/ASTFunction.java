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

import java.util.List;

public class ASTFunction extends GoloASTNode {

  private List<String> arguments;
  private boolean varargs = false;
  private boolean compactForm = false;

  public ASTFunction(int i) {
    super(i);
  }

  public ASTFunction(GoloParser p, int i) {
    super(p, i);
  }

  public List<String> getArguments() {
    return arguments;
  }

  public void setArguments(List<String> arguments) {
    this.arguments = arguments;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  public boolean isCompactForm() {
    return compactForm;
  }

  public void setCompactForm(boolean compactForm) {
    this.compactForm = compactForm;
  }

  @Override
  public String toString() {
    return "ASTFunction{" +
        "arguments=" + arguments +
        ", varargs=" + varargs +
        ", compactForm=" + compactForm +
        '}';
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
