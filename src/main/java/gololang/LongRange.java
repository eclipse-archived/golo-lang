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

class LongRange extends AbstractRange<Long> {

  public LongRange(long from, long to) { super(from, to); }

  public LongRange(long to) { super(to); }

  @Override
  Long defaultValue() { return 0L; }

  @Override
  public Range<Long> reversed() {
    return (new LongRange(to(), from())).decrementBy(increment());
  }

  @Override
  public int size() {
    if (to() == from()) { return 0; }
    int s = (int) ((to() - from()) / increment());
    if (s < 0) { return 0; }
    if (s == 0) { return 1; }
    return s;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Long)) {
      return false;
    }
    Long obj = (Long) o;
    return (encloses(obj) && ((obj - from()) % increment() == 0));
  }

  @Override
  public Range<Long> tail() {
    if (isEmpty()) {
      return this;
    }
    return new LongRange(from() + increment(), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Long> iterator() {
    return new AbstractRange<Long>.RangeIterator() {

      private boolean started = false;
      private long current = from();
      private long to = to();

      @Override
      public boolean hasNext() {
        return Long.compare(to, current) * cmp() > 0;
      }

      @Override
      public Long next() {
        long value = current;
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
