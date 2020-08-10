/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package gololang;

import java.util.Iterator;

/**
 * Wraps a {@code Headtail} into an iterator
 */
public class HeadTailIterator<T> implements Iterator<T> {
  private HeadTail<T> data;

  HeadTailIterator(HeadTail<T> headTail) {
    this.data = headTail;
  }

  @Override
  public boolean hasNext() {
    return !data.isEmpty();
  }

  @Override
  public T next() {
    T h = data.head();
    data = data.tail();
    return h;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("HeadTail object are immutable");
  }
}
