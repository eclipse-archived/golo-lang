/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.NoSuchElementException;

public abstract class GoloElement {
  private WeakReference<GoloASTNode> nodeRef;
  private Optional<GoloElement> parent = Optional.empty();
  private String documentation;

  public void setASTNode(GoloASTNode node) {
    if (node != null) {
      nodeRef = new WeakReference<>(node);
      setDocumentationFrom(node);
    }
  }

  public GoloASTNode getASTNode() {
    if (nodeRef == null) { return null; }
    return nodeRef.get();
  }

  public boolean hasASTNode() {
    return nodeRef != null && nodeRef.get() != null;
  }

  public GoloElement ofAST(GoloASTNode node) {
    if (node != null) {
      node.setIrElement(this);
      setDocumentationFrom(node);
    }
    return this;
  }

  private void setDocumentationFrom(GoloASTNode node) {
    if (node != null && node.getDocumentation() != null) {
      documentation = node.getDocumentation();
    }
  }

  protected void setParentNode(GoloElement parentElement) {
    this.parent = Optional.ofNullable(parentElement);
  }

  public Optional<GoloElement> getParentNode() {
    return this.parent;
  }

  public void makeParentOf(GoloElement childElement) {
    if (childElement != null) {
      childElement.setParentNode(this);
      if (childElement instanceof Scope) {
        Optional<ReferenceTable> referenceTable = this.getLocalReferenceTable();
        if (referenceTable.isPresent()) {
          ((Scope) childElement).relink(referenceTable.get());
        }
      }
    }
  }

  protected RuntimeException cantReplace() {
    return new UnsupportedOperationException(getClass().getName() + " can't replace elements");
  }

  protected RuntimeException cantReplace(GoloElement original, GoloElement replacement) {
    return new IllegalArgumentException(this + " can't replace " + original + " with " + replacement);
  }

  protected RuntimeException doesNotContain(GoloElement element) {
    return new NoSuchElementException(element + " not in " + this);
  }

  protected static RuntimeException cantConvert(String expected, Object value) {
    return new ClassCastException("expecting a " + expected + "but got a " + value.getClass());
  }

  public void replaceInParentBy(GoloElement newElement) {
    if (this.parent.isPresent()) {
      this.parent.get().replaceElement(this, newElement);
      this.parent.get().makeParentOf(newElement);
      if (hasASTNode()) {
        getASTNode().setIrElement(newElement);
      }
      this.setParentNode(null);
    }
  }

  public String getDocumentation() {
    return documentation;
  }

  public PositionInSourceCode getPositionInSourceCode() {
    if (hasASTNode()) {
      return getASTNode().getPositionInSourceCode();
    }
    return new PositionInSourceCode(0, 0);
  }

  public Optional<ReferenceTable> getLocalReferenceTable() {
    if (parent.isPresent()) {
      return parent.get().getLocalReferenceTable();
    }
    return Optional.empty();
  }

  /**
   * Accept the visitor.
   * <p>
   * This method should only call the visitor {@code visitXXXX} method.
   * The children of this node will be visited by the
   * {@link #walk(GoloIrVisitor)} method.
   */
  public abstract void accept(GoloIrVisitor visitor);

  /**
   * Walk the visitor through this node children.
   */
  public abstract void walk(GoloIrVisitor visitor);

  /**
   * Replace a child.
   * <p>
   * Replace {@code original} with {@code newElement} if {@code original} is a child of this node
   * and type matches.
   *
   * @param original the original value to replace. Must be a child of this node
   * @param newElement the element to replace with. Type must match.
   * @throws UnsupportedOperationException if this node does not support replacement
   * @throws NoSuchElementException if {@code original} is not a child of this node
   * @throws ClassCastException if the type of {@code newElement} does not match
   * @see #cantReplace()
   * @see #cantReplace(GoloElement, GoloElement)
   * @see #doesNotContain(GoloElement)
   * @see #cantConvert(String, Object)
   */
  protected abstract void replaceElement(GoloElement original, GoloElement newElement);

}
