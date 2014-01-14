/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.runtime;

import java.util.Iterator;

public class PrimitiveArrayIterator implements Iterator<Object> {

  private final Object[] array;
  private int position = 0;

  public PrimitiveArrayIterator(Object[] array) {
    this.array = array;
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
