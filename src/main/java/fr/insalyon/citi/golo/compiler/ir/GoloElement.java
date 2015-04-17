/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.parser.GoloASTNode;

import java.lang.ref.WeakReference;

public class GoloElement {
  private WeakReference<GoloASTNode> nodeRef;

  public void setASTNode(GoloASTNode node) {
    nodeRef = new WeakReference<>(node);
  }

  public GoloASTNode getASTNode() {
    return nodeRef.get();
  }

  public boolean hasASTNode() {
    return (nodeRef != null) && (nodeRef.get() != null);
  }

  public PositionInSourceCode getPositionInSourceCode() {
    GoloASTNode node = getASTNode();
    if (node == null) {
      return new PositionInSourceCode(0, 0);
    }
    return new PositionInSourceCode(node.jjtGetFirstToken().beginLine, node.jjtGetFirstToken().beginColumn);
  }
}
