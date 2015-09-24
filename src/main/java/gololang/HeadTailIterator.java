/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
