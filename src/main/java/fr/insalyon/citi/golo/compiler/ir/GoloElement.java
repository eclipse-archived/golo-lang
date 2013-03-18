/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.citi.golo.compiler.ir;

import fr.insalyon.citi.golo.compiler.parser.GoloASTNode;
import java.lang.ref.WeakReference;

/**
 *
 * @author david
 */
public class GoloElement {
  private WeakReference<GoloASTNode> nodeRef;
  public void setASTNode(GoloASTNode node) {
    nodeRef = new WeakReference<>(node);
  }

  public GoloASTNode getASTNode() {
    return nodeRef.get();
  }
}
