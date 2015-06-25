/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.parser;

import fr.insalyon.citi.golo.compiler.ir.GoloElement;

public class GoloASTNode extends SimpleNode {

  private GoloElement irElement;
  private String documentation;

  public void setIrElement(GoloElement element) {
    this.irElement = element;

    if (jjtGetFirstToken() != null) {
      // Only add a reverse weak ref to this ASTNode if it was constructed by
      // the parser and is  really part of the AST (on the contrary, temporary
      // AST elements used in the ParseTreeToGoloIR visitor to create IR 
      // elements should not be referenced, since they can be garbage collected
      // at any moment and they don't reflect the source code exactly
      element.setASTNode(this);
    }
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
    return jjtGetFirstToken().beginLine;
  }

  public int getColumnInSourceCode() {
    return jjtGetFirstToken().beginColumn;
  }

  @Override
  public Object jjtAccept(GoloParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }
}
