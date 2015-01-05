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

package gololang;

import java.util.Iterator;
import java.util.NoSuchElementException;

class IntRange extends AbstractRange<Integer> {

  public IntRange(int from, int to) {
    super(from, to);
  }

  public IntRange(int to) {
    super(to);
  }

  @Override
  Integer defaultValue() {
    return 0;
  }

  @Override
  public Range<Integer> reversed() {
    return new IntRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (to() == from()) {
      return 0;
    }
    final int theSize = (to() - from()) / increment();
    if (theSize < 0) {
      return 0;
    }
    if (theSize == 0) {
      return 1;
    }
    return theSize;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Integer)) {
      return false;
    }
    final Integer obj = (Integer) o;
    return encloses(obj) && (obj - from()) % increment() == 0;
  }

  @Override
  public Range<Integer> tail() {
    if (isEmpty()) {
      return this;
    }
    return new IntRange(from() + increment(), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Integer> iterator() {
    return new AbstractRange<Integer>.RangeIterator() {

      private boolean started = false;
      private int current = from();
      private int to = to();

      @Override
      public boolean hasNext() {
        return Integer.compare(to, current) * cmp() > 0;
      }

      @Override
      public Integer next() {
        final int value = current;
        if (started) {
          if (hasNext()) {
            current = current + increment();
            return value;
          } else {
            throw new NoSuchElementException("iteration has finished");
          }
        } else {
          started = true;
          current = current + increment();
          return value;
        }
      }
    };
  }
}
