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

import java.util.Optional;
import java.util.NoSuchElementException;

public abstract class GoloElement<T extends GoloElement<T>> {
  private GoloElement<?> parent;
  private String documentation;
  private PositionInSourceCode position;

  protected abstract T self();

  public T ofAST(GoloASTNode node) {
    if (node != null) {
      this.documentation(node.getDocumentation());
      this.positionInSourceCode(node.getPositionInSourceCode());
    }
    return self();
  }

  protected void setParentNode(GoloElement<?> parentElement) {
    this.parent = parentElement;
  }

  public boolean hasParent() {
    return this.parent != null;
  }

  public GoloElement<?> parent() {
    return this.parent;
  }

  public <C extends GoloElement<?>> C makeParentOf(C childElement) {
    if (childElement != null) {
      childElement.setParentNode(this);
      relinkChild(childElement);
    }
    return childElement;
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

  protected RuntimeException cantReplace(GoloElement<?> original, GoloElement<?> replacement) {
    return new IllegalArgumentException(this + " can't replace " + original + " with " + replacement);
  }

  protected RuntimeException doesNotContain(GoloElement<?> element) {
    return new NoSuchElementException(element + " not in " + this);
  }

  protected static RuntimeException cantConvert(String expected, Object value) {
    return new ClassCastException(String.format(
          "expecting a %s but got a %s",
          expected,
          value == null ? "null value" : value.getClass()));
  }

  /**
   * Replaces this element by the given one in its parent node.
   *
   * @param newElement the element to replace this one with.
   * @throws IllegalStateException if this element has no parent.
   */
  public final void replaceInParentBy(GoloElement<?> newElement) {
    if (newElement == this) { return; }
    if (this.parent != null) {
      this.parent.replaceElement(this, newElement);
      this.parent.makeParentOf(newElement);
      if (newElement.position == null) {
        newElement.position = this.position;
      }
      if (newElement.documentation == null || newElement.documentation.isEmpty()) {
        newElement.documentation = this.documentation;
      }
      this.setParentNode(null);
    } else {
      throw new IllegalStateException("This node has no parent");
    }
  }

  public String documentation() {
    return this.documentation;
  }

  public T documentation(String doc) {
    if (doc != null) {
      this.documentation = doc;
    }
    return self();
  }

  public PositionInSourceCode positionInSourceCode() {
    if (this.position == null && this.parent != null) {
      return this.parent.positionInSourceCode();
    }
    return PositionInSourceCode.of(this.position);
  }

  public T positionInSourceCode(PositionInSourceCode pos) {
    if (pos != null && pos.isUndefined()) {
      this.position = null;
    } else {
      this.position = pos;
    }
    return self();
  }

  public boolean hasPosition() {
    return position != null && !position.isUndefined();
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
  protected abstract void replaceElement(GoloElement<?> original, GoloElement<?> newElement);

}
