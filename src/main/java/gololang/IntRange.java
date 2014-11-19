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
import java.util.Arrays;

class IntRange extends AbstractRange<Integer> {

  public IntRange(int from, int to) {
    super(from, to);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new AbstractRange.RangeIterator() {

      private boolean started = false;
      private int current = from();
      private int to = to();

      @Override
      public boolean hasNext() {
        return Integer.compare(to, current) == cmp();
      }

      @Override
      public Integer next() {
        int value = current;
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
