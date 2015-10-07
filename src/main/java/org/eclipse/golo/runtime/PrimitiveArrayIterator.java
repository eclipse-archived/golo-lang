/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.util.Iterator;

public class PrimitiveArrayIterator implements Iterator<Object> {

  private final Object[] array;
  private int position = 0;

  public PrimitiveArrayIterator(Object[] array) {
    if (array == null) {
      this.array = new Object[0];
    } else {
      this.array = java.util.Arrays.copyOf(array, array.length);
    }
  }

  @Override
  public boolean hasNext() {
    return position < array.length;
  }

  @Override
  public Object next() {
    if (hasNext()) {
      return array[position++];
    } else {
      throw new ArrayIndexOutOfBoundsException(position);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
