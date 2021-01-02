/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.golo.compiler.PositionInSourceCode;

import static java.util.stream.Collectors.toList;

/**
 * Generic IR tree Element.
 * <p>
 * The IR tree represents a more abstract representation of the Golo code.
 */
public abstract class GoloElement<T extends GoloElement<T>> {
  private GoloElement<?> parent;
  private String documentation;
  private PositionInSourceCode position;
  private final Map<String, Object> meta = new HashMap<>();

  protected abstract T self();

  /**
   * Init the position and documentation from the given AST node.
   * <p>
   *
   * @param node the {@link GoloASTNode} to reference.
   */
  public final T ofAST(GoloASTNode node) {
    if (node != null) {
      this.documentation(node.getDocumentation());
      this.positionInSourceCode(node.getPositionInSourceCode());
    }
    return self();
  }

  private void setParentNode(GoloElement<?> parentElement) {
    this.parent = parentElement;
  }

  public final boolean hasParent() {
    return this.parent != null && this.parent != this;
  }

  public final GoloElement<?> parent() {
    return this.parent;
  }

  /**
   * Returns the module containing this element.
   */
  public GoloModule enclosingModule() {
    if (this.parent == null) {
      return null;
    }
    return this.parent.enclosingModule();
  }

  /**
   * Returns the first ancestor of this node being an instance of the given class.
   *
   * Note that if the ancestor can be the element itself.
   */
  public <C extends GoloElement<?>> C ancestorOfType(Class<C> cls) {
    return cls.cast(ancestor(cls::isInstance));
  }

  /**
   * Returns the first ancestor of this node matching the given predicate.
   *
   * Note that if the ancestor can be the element itself.
   */
  public GoloElement<?> ancestor(Predicate<GoloElement<?>> predicate) {
    if (this.parent == null) {
      return null;
    }
    if (predicate.test(this.parent)) {
      return this.parent;
    }
    if (this.parent == this) {
      // Not tested at the same time as `null` since if `this` matches the predicate it is returned.
      return null;
    }
    return this.parent.ancestor(predicate);
  }
  /**
   * Returns the first ancestor of this node matching the given predicate.
   *
   * Note that if the ancestor can be the element itself.
   */
  public GoloElement<?> ancestor(gololang.FunctionReference predicate) {
    // NOTE: this implementation is required due to some bugs in the implicit conversion of golo function references
    // into java lambda (see https://github.com/eclipse/golo-lang/issues/277)
    // When resolved, the `ancestor(Predicate<GoloElement>)` will suffice.
    return ancestor(predicateWrapper(predicate));
  }

  /**
   * Returns a list of all the descendants of this node matching the given predicate.
   */
  public List<GoloElement<?>> descendants(Predicate<GoloElement<?>> predicate) {
    return descendants().filter(predicate).collect(toList());
  }

  /**
   * Returns a list of all the descendants of this node matching the given predicate.
   */
  public List<GoloElement<?>> descendants(gololang.FunctionReference predicate) {
    // NOTE: this implementation is required due to some bugs in the implicit conversion of golo function references
    // into java lambda (see https://github.com/eclipse/golo-lang/issues/277)
    // When resolved, the `descendants(Predicate<GoloElement>)` will suffice.
    return descendants(predicateWrapper(predicate));
  }

  public Stream<GoloElement<?>> descendants() {
    return Stream.concat(
        children().stream(),
        children().stream().flatMap(GoloElement::descendants));
  }

  private static Predicate<GoloElement<?>> predicateWrapper(gololang.FunctionReference predicate) {
    return n -> {
      try {
        return (Boolean) predicate.invoke(n);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Returns a list of all the direct children of this node.
   */
  public List<GoloElement<?>> children() {
    return Collections.emptyList();
  }

  /**
   * Returns a list the direct children of this node matching the given predicate.
   */
  public List<GoloElement<?>> children(Predicate<GoloElement<?>> predicate) {
    return children().stream().filter(predicate).collect(toList());
  }

  /**
   * Returns a iterable on the direct children of this node matching the given predicate.
   */
  public List<GoloElement<?>> children(gololang.FunctionReference predicate) {
    // NOTE: this implementation is required due to some bugs in the implicit conversion of golo function references
    // into java lambda (see https://github.com/eclipse/golo-lang/issues/277)
    // When resolved, the `children(Predicate<GoloElement>)` will suffice.
    return children(predicateWrapper(predicate));
  }

  /**
   * Returns the previous sibling of this element.
   */
  public GoloElement<?> previous() {
    return previous(e -> true);
  }

  /**
   * Returns the previous sibling of this element matching the given predicate.
   */
  public GoloElement<?> previous(Predicate<GoloElement<?>> predicate) {
    if (this.parent == null || this.parent == this) {
      return null;
    }
    GoloElement<?> previous = null;
    for (GoloElement<?> e :  this.parent.children()) {
      if (e == this) {
        break;
      }
      if (predicate.test(e)) {
        previous = e;
      }
    }
    return previous;
  }


  /**
   * Returns the previous sibling of this element matching the given predicate.
   */
  public GoloElement<?> previous(gololang.FunctionReference predicate) {
    // NOTE: this implementation is required due to some bugs in the implicit conversion of golo function references
    // into java lambda (see https://github.com/eclipse/golo-lang/issues/277)
    return previous(predicateWrapper(predicate));
  }

  /**
   * Returns the next sibling of this element.
   */
  public GoloElement<?> next() {
    return next(e -> true);
  }

  /**
   * Returns the next sibling of this element matching the given predicate.
   */
  public GoloElement<?> next(gololang.FunctionReference predicate) {
    // NOTE: this implementation is required due to some bugs in the implicit conversion of golo function references
    // into java lambda (see https://github.com/eclipse/golo-lang/issues/277)
    return next(predicateWrapper(predicate));
  }

  /**
   * Returns the next sibling of this element matching the given predicate.
   */
  public GoloElement<?> next(Predicate<GoloElement<?>> predicate) {
    if (this.parent == null || this.parent == this) {
      return null;
    }
    boolean found = false;
    for (GoloElement<?> e :  this.parent.children()) {
      if (found && predicate.test(e)) {
        return e;
      }
      if (e == this) {
        found = true;
      }
    }
    return null;
  }

  protected final <C extends GoloElement<?>> C makeParentOf(C childElement) {
    if (childElement != null && childElement.parent() != this) {
      ((GoloElement<?>) childElement).setParentNode(this);
      relinkChild(childElement);
    }
    return childElement;
  }

  private void relinkChild(GoloElement<?> child) {
    this.getLocalReferenceTable().ifPresent((rt) -> child.accept(new RelinkIrVisitor(rt)));
  }

  /**
   * Retrieve a previously stored meta-data.
   */
  public final Object metadata(String name) {
    return this.meta.get(name);
  }

  /**
   * Retrieve metadata, searching all element hierarchy.
   */
  public final Object inheritedMetadata(String name) {
    Object m = this.meta.get(name);
    if (m != null || this.parent == null || this.parent == this) {
      return m;
    }
    return this.parent.inheritedMetadata(name);
  }

  /**
   * Stores a meta-data in this element.
   * <p>
   * A meta-data can be any object. The main purpose is to allow visitors or macros to store some generic informations in the IR
   * that can be used later by other macros or compilation steps (for instance add some Java annotations to the
   * generated methods).
   */
  public final T metadata(String name, Object value) {
    if (value == null) {
      this.meta.remove(name);
    } else {
      this.meta.put(name, value);
    }
    return self();
  }

  protected final RuntimeException cantReplace() {
    return new UnsupportedOperationException(getClass().getName() + " can't replace elements");
  }

  protected final RuntimeException cantReplace(GoloElement<?> original, GoloElement<?> replacement) {
    return new IllegalArgumentException(this + " can't replace " + original + " with " + replacement);
  }

  protected final RuntimeException doesNotContain(GoloElement<?> element) {
    return new NoSuchElementException(element + " not in " + this);
  }

  protected static final RuntimeException cantConvert(String expected, Object value) {
    return new ClassCastException(String.format(
          "expecting a %s but got a %s",
          expected,
          value == null ? "null value" : value.getClass().getName()));
  }

  /**
   * Replaces this element by the given one in its parent node.
   * <p>
   * This method is typically used during the sugar and macros expansion stages to replace the element by a desugarized version,
   * or the macro call by its result.
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

  public final String documentation() {
    return this.documentation;
  }

  public final T documentation(String doc) {
    if (doc != null) {
      this.documentation = doc;
    }
    return self();
  }

  public final PositionInSourceCode positionInSourceCode() {
    if (this.position == null && this.parent != null) {
      return this.parent.positionInSourceCode();
    }
    return PositionInSourceCode.of(this.position);
  }

  public final T positionInSourceCode(PositionInSourceCode pos) {
    if (pos != null && pos.isUndefined()) {
      this.position = null;
    } else {
      this.position = pos;
    }
    return self();
  }

  public final boolean hasPosition() {
    return position != null || (this.parent != null && this.parent != this && this.parent.hasPosition());
  }

  public Optional<ReferenceTable> getLocalReferenceTable() {
    if (hasParent()) {
      return this.parent.getLocalReferenceTable();
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
  public void walk(GoloIrVisitor visitor) {
    for (GoloElement<?> e : children()) {
      e.accept(visitor);
    }
  }

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

  private static class RelinkIrVisitor extends AbstractGoloIrVisitor {
    private final ReferenceTable rt;
    boolean prune;

    RelinkIrVisitor(ReferenceTable rt) {
      this.rt = rt;
      prune = true;
    }

    @Override
    public void visitBlock(Block b) {
      b.getReferenceTable().relink(rt, prune);
      // We don't walk the subtree since contained blocks are already linked to this,
      // block, and thus to `rt` by transitivity.
    }

    @Override
    public void visitClosureReference(ClosureReference c) {
      prune = false;
      c.walk(this);
    }

    @Override
    public void visitMacroInvocation(MacroInvocation macroInvocation) {
      macroInvocation.walk(this);
    }
  }
}
