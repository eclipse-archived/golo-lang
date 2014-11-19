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
import java.util.Arrays;

abstract class AbstractRange<T> implements Range<T> {
  private final T from;
  private final T to;
  private int increment = 1;
  private int cmp = 1;

  abstract class RangeIterator<E> implements Iterator<E> {

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is not supported on a range");
    }

  }

  public AbstractRange(T from, T to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public T from() {
    return this.from;
  }

  @Override
  public T to() {
    return this.to;
  }

  @Override
  public int increment() {
    return this.increment;
  }

  protected int cmp() {
    return this.cmp;
  }

  @Override
  public Range<T> incrementBy(int value) {
    this.increment = value;
    if (value < 0) {
      this.cmp = -1;
    } else {
      this.cmp = 1;
    }
    return this;
  }

  @Override
  public Range<T> decrementBy(int value) {
    return this.incrementBy(-value);
  }

  @Override
  public String toString() {
    if (this.increment != 1) {
      return String.format("range(%s,%s):incrementBy(%s)", this.from, this.to, this.increment);
    }
    return String.format("range(%s,%s)", this.from, this.to);
  }

  @Override
  public boolean equals(Object other) {
    return (
      other instanceof Range
      && this.from().equals(((Range)other).from())
      && this.to().equals(((Range)other).to())
      && this.increment() == ((Range)other).increment()
    );
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new int[]{
      this.from().hashCode(),
      this.to().hashCode(),
      this.increment()}
    );
  }
}
