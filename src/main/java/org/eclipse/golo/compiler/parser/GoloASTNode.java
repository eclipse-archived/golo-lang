/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

import org.eclipse.golo.compiler.PositionInSourceCode;

public class GoloASTNode extends SimpleNode {

  private String documentation;

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
    Token firstToken = this.jjtGetFirstToken();
    if (firstToken == null) {
      return PositionInSourceCode.undefined();
    }
    int startLine = firstToken.beginLine;
    int startColumn = firstToken.beginColumn;
    int endLine = firstToken.endLine;
    int endColumn = firstToken.endColumn;
    Token lastToken = this.jjtGetLastToken();
    if (lastToken != null) {
      endLine = lastToken.endLine;
      endColumn = lastToken.endColumn;
    }
    return PositionInSourceCode.of(startLine, startColumn, endLine, endColumn);
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
