/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.NoSuchElementException;

public abstract class GoloElement {
  private WeakReference<GoloASTNode> nodeRef;
  private GoloElement parent;
  private String documentation;
  private PositionInSourceCode position = PositionInSourceCode.UNDEFINED;

  public GoloASTNode getASTNode() {
    if (nodeRef == null) { return null; }
    return nodeRef.get();
  }

  private boolean hasASTNode() {
    return nodeRef != null && nodeRef.get() != null;
  }

  public GoloElement ofAST(GoloASTNode node) {
    if (node != null) {
      nodeRef = new WeakReference<>(node);
      node.setIrElement(this);
      this.documentation(node.getDocumentation());
      this.positionInSourceCode(node.getPositionInSourceCode());
    }
    return this;
  }

  protected void setParentNode(GoloElement parentElement) {
    this.parent = parentElement;
  }

  public Optional<GoloElement> getParentNode() {
    return Optional.ofNullable(this.parent);
  }

  public void makeParentOf(GoloElement childElement) {
    if (childElement != null) {
      childElement.setParentNode(this);
      relinkChild(childElement);
    }
  }

  private void relinkChild(GoloElement child) {
    this.getLocalReferenceTable().ifPresent((rt) -> child.accept(new AbstractGoloIrVisitor() {
      boolean prune = true;
      @Override
      public void visitBlock(Block b) {
        b.getReferenceTable().relink(rt, prune);
      }

      @Override
      public void visitClosureReference(ClosureReference c) {
        prune = false;
        c.walk(this);
      }
    }));
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

  /**
   * Replaces this element by the given one in its parent node.
   *
   * @param newElement the element to replace this one with.
   * @throws IllegalStateException if this element has no parent.
   */
  public void replaceInParentBy(GoloElement newElement) {
    if (newElement == this) { return; }
    if (this.parent != null) {
      this.parent.replaceElement(this, newElement);
      this.parent.makeParentOf(newElement);
      newElement.positionInSourceCode(this.position);
      this.setParentNode(null);
    } else {
      throw new IllegalStateException("This node has no parent");
    }
  }

  public String documentation() {
    return this.documentation;
  }

  public GoloElement documentation(String doc) {
    if (doc != null) {
      this.documentation = doc;
    }
    return this;
  }

  public PositionInSourceCode positionInSourceCode() {
    return position;
  }

  public GoloElement positionInSourceCode(PositionInSourceCode pos) {
    if (pos == null) {
      this.position = PositionInSourceCode.UNDEFINED;
    } else {
      this.position = pos;
    }
    return this;
  }

  public GoloElement positionInSourceCode(int line, int column) {
    if (line <= 0 && column <= 0) {
      return positionInSourceCode(null);
    }
    return positionInSourceCode(new PositionInSourceCode(line, column));
  }

  public boolean hasPosition() {
    return !PositionInSourceCode.UNDEFINED.equals(position);
  }

  public Optional<ReferenceTable> getLocalReferenceTable() {
    if (this.parent != null) {
      return parent.getLocalReferenceTable();
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
