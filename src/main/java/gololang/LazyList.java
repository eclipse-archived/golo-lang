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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Represents a lazy list object.
 * <p>
 * A lazy list behaves like a linked list, but each next element
 * is represented by a closure that is evaluated only if needed.
 */
public final class LazyList implements Collection<Object> {

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
  @Override
  public boolean isEmpty() {
    return this.head == null;
  }

  /**
   * Creates an iterator over the list.
   * <p>The iterator does not support removal.
   *
   * @return an iterator.
   */
  @Override
  public Iterator<Object> iterator() {
    return new LazyListIterator(this);
  }

  /**
   * Convert the lazy list into a regular list.
   * <p>
   * Note that it evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return a new {@code LinkedList}
   */
  public List<Object> asList() {
    List<Object> lst = new LinkedList();
    for (Object o : this) {
      lst.add(o);
    }
    return lst;
  }

  /**
   * Returns the number of elements in this list.
   * <p>
   * Note that it evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   * 
   * @return the number of elements in this list.
   */
  @Override
  public int size() {
    if (this.isEmpty()) return 0;
    return 1 + this.tail().size();
  }

  /**
   * Compares the specified object with this list.
   * <p>
   * This is a value comparison.
   * Note that it evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param o the object to be compared for equality with this list
   * @return {@code true} if the specified object is equal to this list.
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LazyList)) return false;
    LazyList other = (LazyList)o;
    if (this.isEmpty() && other.isEmpty()) return true;
    return (this.head().equals(other.head()) && this.tail().equals(other.tail()));
  }

  /**
   * Returns an array containing all of the elements in this list.
   * <p>
   * Note that it evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return an array containing all of the elements in this list
   */
  @Override
  public Object[] toArray() {
    return this.asList().toArray();
  }

  /**
   * Returns an array containing all of the elements in this list with a type
   * of the given array.
   * <p>
   * Note that it evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return an array containing all of the elements in this list
   */
  @Override
  public <T> T[] toArray(T[] a) {
    return this.asList().toArray(a);
  }

  /**
   * Returns the element at the specified position in this list.
   * <p>
   * Note that this evaluate the list up to the required element.
   *
   * @param index index of the element to return
   * @return the element at the specified position in this list
   */
  public Object get(int index) {
    if (index < 0 || this.isEmpty()) throw new IndexOutOfBoundsException();
    if (index == 0) return this.head();
    return this.tail().get(index - 1);
  }

  /**
   * Returns the position of the first occurence of the given element in the
   * list.
   * <p>
   * Note that this evaluate the list up to the given element. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param o element to search for
   * @return the index of the first occurence, or -1 if not present
   */
  public int indexOf(Object o) {
    int idx = 0;
    for (Object elt : this) {
      if (elt.equals(o)) return idx;
      idx++;
    }
    return -1;
  }

  /**
   * Check if the list contains the given object.
   * <p>
   * Note that this evaluate the list up to the given element. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param o element to search for
   * @return {@code true} if the element is in the list, {@code false}
   * otherwise.
   */
  @Override
  public boolean contains(Object o) {
    return this.indexOf(o) != -1;
  }

  /**
   * Check if the list contains all the objects in the given collection.
   * <p>
   * Note that this evaluate the list up to the given element, *for each*
   * element in the collection (at worse). This implementation is highly inefficient.
   * <p>
   * Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param c collection of elements to search for
   * @return {@code true} if all the elements are in the list, {@code false}
   * otherwise.
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object elt : c) {
      if (!this.contains(elt)) return false;
    }
    return true;
  }

  @Override
  public boolean add(Object e) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  public void add(int index, Object element) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }
  
  @Override
  public boolean addAll(Collection<?> c) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  public boolean addAll(int index, Collection<?> c) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  @Override
  public boolean remove(Object e) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  public Object remove(int index) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  @Override
  public boolean retainAll(Collection<?> c){
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

  public Object set(int index, Object element) {
    throw new UnsupportedOperationException("a LazyList is immutable");
  }

}
