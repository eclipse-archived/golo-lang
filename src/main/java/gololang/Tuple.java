/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package gololang;

import java.util.Arrays;
import java.util.Iterator;

public final class Tuple implements Iterable<Object> {

  private final Object[] data;

  public Tuple(Object... values) {
    data = Arrays.copyOf(values, values.length);
  }

  public int size() {
    return data.length;
  }

  public Object get(int index) {
    if (index < 0 || index >= data.length) {
      throw new IndexOutOfBoundsException(index + " is outside the bounds of a " + data.length + "-tuple");
    }
    return data[index];
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iterator<Object>() {

      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < data.length;
      }

      @Override
      public Object next() {
        Object result = data[i];
        i = i + 1;
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Tuples are immutable");
      }
    };
  }
}
