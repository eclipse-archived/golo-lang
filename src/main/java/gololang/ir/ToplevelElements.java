/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import gololang.FunctionReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A container of top-level {@code GoloElement}.
 *
 * <p>This element must never be present in a tree.
 */
public final class ToplevelElements extends GoloElement<ToplevelElements> implements Iterable<GoloElement<?>> {

  private final LinkedHashSet<GoloElement<?>> elements = new LinkedHashSet<>();

  private ToplevelElements(GoloElement<?>... elements) {
    for (GoloElement<?> e : elements) {
      this.add(e);
    }
  }

  /**
   * Creates a top-level elements container.
   */
  public static ToplevelElements of(Object... elements) {
    ToplevelElements tl = new ToplevelElements();
    for (Object e : elements) {
      tl.add(e);
    }
    return tl;
  }

  protected ToplevelElements self() { return this; }

  public ToplevelElements add(Object element) {
    if (element instanceof ToplevelElements) {
      for (GoloElement<?> e : (ToplevelElements) element) {
        this.add(e);
      }
    } else if (element instanceof ToplevelGoloElement && !(element instanceof Noop)) {
      GoloElement<?> elt = (GoloElement<?>) element;
      this.elements.add(makeParentOf(elt));
    } else {
      throw new IllegalArgumentException(element.toString());
    }
    return this;
  }

  public boolean isEmpty() {
    return this.elements.isEmpty();
  }

  /**
   * Map a golo function on the contained elements.
   */
  public ToplevelElements map(FunctionReference fun) throws Throwable {
    ToplevelElements res = new ToplevelElements();
    for (GoloElement<?> elt : this) {
      res.add(fun.invoke(elt));
    }
    return res;
  }

  @Override
  public Iterator<GoloElement<?>> iterator() {
    return elements.iterator();
  }

  @Override
  public List<GoloElement<?>> children() {
    return new ArrayList<>(elements);
  }

  @Override
  public void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (elements.contains(original)) {
      elements.remove(original);
      add(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitToplevelElements(this);
  }
}

