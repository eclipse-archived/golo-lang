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
    if (jjtGetFirstToken() != null) {
      return new PositionInSourceCode(getLineInSourceCode(), getColumnInSourceCode());
    }
    return null;
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
