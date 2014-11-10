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

import java.lang.invoke.MethodHandle;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Represents a lazy list object.
 * <p>
 * A lazy list behaves like a linked list, but each next element
 * is represented by a closure that is evaluated only if needed.
 */
public final class LazyList implements Iterable<Object> {

  /**
   * Represents the empty list.
   */
  public static final LazyList EMPTY = new LazyList(null, null);

  /**
   * Iterator over a {@code LazyList}.
   */
  class LazyListIterator implements Iterator<Object> {

    private LazyList list;

    LazyListIterator(LazyList aLazyList) {
      this.list = aLazyList;
    }

    @Override
    public boolean hasNext() {
      return !list.isEmpty();
    }

    @Override
    public Object next() {
      Object h = list.head();
      list = list.tail();
      return h;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "remove() is not supported on a lazy list iterator");
    }
  }

  private Object head;
  private MethodHandle tail;

  /**
   * Create a new list from the head and tail values.
   *
   * @param head the first value of the list.
   * @param tail a {@code MethodHandle} that returns a LazyList when invoked.
   */
  public LazyList(Object head, MethodHandle tail) {
    this.head = head;
    this.tail = tail;
  }

  /**
   * Gets the first element of the list (its head).
   *
   * @return an {@code Object}, or {@code null} if the list is empty.
   */
  public Object head() {
    return this.head;
  }

  /**
   * Gets the rest of the list (its tail).
   *
   * @return a {@code LazyList}, or {@code EMPTY} if the list is empty,
   * contains only one value, or if the closure failed.
   */
  public LazyList tail() {
    try {
      return (LazyList) (this.tail.invokeWithArguments());
    } catch (Throwable e) {
      return EMPTY;
    }
  }

  /**
   * Checks whether the list is empty or not.
   *
   * @return {@code true} if the list has no element, {@code false} otherwise.
   */
  public boolean isEmpty() {
    return this.head == null;
  }

  /**
   * Creates an iterator over the list.
   * <p>The iterator does not support removal.
   *
   * @return an iterator.
   */
  public Iterator<Object> iterator() {
    return new LazyListIterator(this);
  }

  /**
   * Convert the lazy list into a regular list
   *
   * @return a list
   */
  public List<Object> asList() {
    List<Object> lst = new LinkedList();
    for (Object o : this) {
      lst.add(o);
    }
    return lst;
  }

  //TODO: equals(Object other)
  //TODO: get(int index)
  //TODO: indexOf(Object o)
  //TODO: contains(Object o)
  //TODO: toArray()
}
