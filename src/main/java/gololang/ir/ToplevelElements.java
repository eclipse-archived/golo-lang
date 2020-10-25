/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
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
 * <p>
 * This class is mainly used by top-level macros to return a collection of golo top-level elements,
 * i.e. functions, structs, augments and so on, since a macro must return a unique GoloElement to
 * inject in the Ir by replacing the macro call.
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
   *
   * <p>Mainly used to return several nodes from a top-level macro.
   * <p>If only a {@code ToplevelElements} instance is given, it is returned unchanged.
   */
  public static ToplevelElements of(Object... elements) {
    if (elements.length == 1 && elements[0] instanceof ToplevelElements) {
      return (ToplevelElements) elements[0];
    }
    if (elements.length == 1 && elements[0] instanceof Iterable) {
      return fromIterable((Iterable<Object>) elements[0]);
    }
    ToplevelElements tl = new ToplevelElements();
    for (Object e : elements) {
      tl.add(e);
    }
    return tl;
  }

/**
   * Creates a top-level elements container.
   *
   * <p>Mainly used to return several nodes from a top-level macro.
   */
  public static ToplevelElements fromIterable(Iterable<Object> elements) {
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
   * <p>
   * This can be used in top-level macros to apply the macro on a top-level from a previous macro application, for
   * instance when stacking decorator-like macros returning a {@code ToplevelElements}.
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

