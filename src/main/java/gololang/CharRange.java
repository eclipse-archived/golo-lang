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

/**
 * Defines a range object on Character.
 */
final class CharRange extends AbstractRange<Character> {

  public CharRange(char from, char to) {
    super(from, to);
  }

  public CharRange(char to) {
    super(to);
  }

  @Override
  Character defaultValue() {
    return 'A';
  }

  @Override
  public Range<Character> reversed() {
    return new CharRange(to(), from()).decrementBy(increment());
  }

  @Override
  public int size() {
    if (to() == from()) {
      return 0;
    }
    final int s = ((int) to().charValue() - (int) from().charValue()) / increment();
    if (s < 0) {
      return 0;
    }
    if (s == 0) {
      return 1;
    }
    return s;
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof Character)) {
      return false; 
    }
    final Character obj = (Character) o;
    return encloses(obj)
           && ((int) obj.charValue() - (int) from().charValue()) % increment() == 0;
  }

  @Override
  public Range<Character> tail() {
    if (isEmpty()) {
      return this;
    }
    return new CharRange((char) (from() + increment()), to()).incrementBy(increment());
  }

  @Override
  public Iterator<Character> iterator() {
    return new AbstractRange<Character>.RangeIterator() {

      private boolean started = false;
      private char current = from().charValue();
      private char to = to().charValue();

      @Override
      public boolean hasNext() {
        return Character.compare(to, current) * cmp() > 0;
      }

      @Override
      public Character next() {
        final Character value = Character.valueOf(current);
        if (started && !hasNext()) {
          throw new NoSuchElementException("iteration has finished");
        } else {
          started = true;
        }
        current = (char) (current + increment());
        return value;
      }
    };
  }
}
