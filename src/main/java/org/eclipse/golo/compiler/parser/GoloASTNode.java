/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

import org.eclipse.golo.compiler.ir.GoloElement;
import org.eclipse.golo.compiler.ir.PositionInSourceCode;

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

  public PositionInSourceCode getPositionInSourceCode() {
    return new PositionInSourceCode(getLineInSourceCode(), getColumnInSourceCode());
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
