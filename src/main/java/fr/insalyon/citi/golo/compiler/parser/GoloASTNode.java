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

import fr.insalyon.citi.golo.compiler.ir.GoloElement;
        
public class GoloASTNode extends SimpleNode {

  private int lineInSourceCode = -1;
  private int columnInSourceCode = -1;
  private GoloElement irElement;

  public void setIrElement(GoloElement element) {
    this.irElement = element;
    element.setASTNode(this);
  }

  public GoloElement getIrElement() {
    return irElement;
  }

  public GoloASTNode(int i) {
    super(i);
  }

  public GoloASTNode(GoloParser p, int i) {
    super(p, i);
  }

  public int getLineInSourceCode() {
    return lineInSourceCode;
  }

  public void setLineInSourceCode(int lineInSourceCode) {
    this.lineInSourceCode = lineInSourceCode;
  }

  public int getColumnInSourceCode() {
    return columnInSourceCode;
  }

  public void setColumnInSourceCode(int columnInSourceCode) {
    this.columnInSourceCode = columnInSourceCode;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
