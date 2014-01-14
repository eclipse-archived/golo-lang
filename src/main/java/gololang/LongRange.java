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

class LongRange implements Iterable<Long> {

  private final long from;
  private final long to;
  private long increment = 1;

  public long from() {
    return this.from;
  }

  public long to() {
    return this.to;
  }

  public long increment() {
    return this.increment;
  }

  public LongRange(long from, long to) {
    this.from = from;
    this.to = to;
  }

  public LongRange incrementBy(long value) {
    this.increment = value;
    return this;
  }

  @Override
  public Iterator<Long> iterator() {
    return new Iterator<Long>() {

      private boolean started = false;
      private long current = from;

      @Override
      public boolean hasNext() {
        return current < to;
      }

      @Override
      public Long next() {
        long value = current;
        if (started) {
          if (hasNext()) {
            current = current + increment;
            return value;
          } else {
            throw new NoSuchElementException("iteration has finished");
          }
        } else {
          started = true;
          current = current + increment;
          return value;
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove() is not supported on a range");
      }
    };
  }
}
