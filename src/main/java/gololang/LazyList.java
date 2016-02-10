/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Represents a lazy list object.
 * <p>
 * A lazy list behaves like a linked list, but each next element
 * is represented by a closure that is evaluated only if needed.
 * The value is cached, so that the closure representing the tail
 * is evaluated only once.
 *
 * Since the tail closure will be called at most once, and we can't
 * guarantee when, or even if, it will be called, this closure must be
 * a pure, side-effect free, function.
 */
public class LazyList implements Collection<Object>, HeadTail<Object> {

  /**
   * Represents the empty list.
   */
  public static final LazyList EMPTY = new LazyList(null, null) {
    @Override
    public boolean equals(Object other) {
      return other == this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(null, null);
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public LazyList tail() {
      return this;
    }

    @Override
    public String toString() {
      return "LazyList.EMPTY";
    }
  };

  private final Object head;
  private final FunctionReference tail;
  private LazyList memoTail = null;

  /**
   * Create a new list from the head and tail values.
   *
   * @param head the first value of the list.
   * @param tail a {@code FunctionReference} that returns a LazyList when invoked.
   * @return a new {@code LazyList}
   */
  public static LazyList cons(Object head, FunctionReference tail) {
    if (tail == null) {
      throw new IllegalArgumentException("Use the empty list instead of null as the last element of a LazyList");
    }
    return new LazyList(head, tail);
  }

  private LazyList(Object head, FunctionReference tail) {
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
    if (memoTail == null) {
      try {
        memoTail = (LazyList) (this.tail.invoke());
      } catch (Throwable e) {
        memoTail = EMPTY;
      }
    }
    return memoTail;
  }

  /**
   * Checks whether the list is empty or not.
   *
   * @return {@code true} if the list is EMPTY, {@code false} otherwise.
   */
  @Override
  public boolean isEmpty() {
    return false;
  }

  /**
   * Creates an iterator over the list.
   * <p>The iterator does not support removal.
   *
   * @return an iterator.
   */
  @Override
  public Iterator<Object> iterator() {
    return new HeadTailIterator<Object>(this);
  }

  /**
   * Convert the lazy list into a regular list.
   * <p>
   * Note that it evaluates the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return a new {@code LinkedList}
   */
  public List<Object> asList() {
    List<Object> lst = new LinkedList<>();
    for (Object o : this) {
      lst.add(o);
    }
    return lst;
  }

  /**
   * Returns the number of elements in this list.
   * <p>
   * Note that it evaluates the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return the number of elements in this list.
   */
  @Override
  public int size() {
    return 1 + this.tail().size();
  }

  /**
   * Compares the specified object with this list.
   * <p>
   * This is a value comparison.
   * Note that it may evaluate the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param o the object to be compared for equality with this list
   * @return {@code true} if the specified object is equal to this list.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof LazyList)) return false;
    LazyList other = (LazyList) o;
    if (this.isEmpty() && other.isEmpty()) return true;
    if (!this.head.equals(other.head)) return false;
    if (this.tail.equals(other.tail)) return true;
    return this.tail().equals(other.tail());
  }

  /**
   * Compute the hashCode of this list.
   * <p>
   * Note that it evaluates the whole list. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @return the {@code hashCode}.
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.head, this.tail());
  }

  /**
   * Returns an array containing all of the elements in this list.
   * <p>
   * Note that it evaluates the whole list. Take care to
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
   * Note that it evaluates the whole list. Take care to
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
   * Destructuration helper.
   *
   * @return a tuple of head and tail
   */
  public Tuple destruct() {
    return new Tuple(head(), tail());
  }

  /**
   * Returns the element at the specified position in this list.
   * <p>
   * Note that it evaluates the list up to the required element.
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
   * Returns the position of the first occurrence of the given element in the
   * list.
   * <p>
   * Note that it evaluates the list up to the given element. Take care to
   * <b>not use</b> this method on infinite lists, since
   * no check is done.
   *
   * @param o element to search for
   * @return the index of the first occurrence, or -1 if not present
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
   * Note that it evaluates the list up to the given element. Take care to
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
   * Note that it evaluates the list up to the given element, *for each*
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
  public String toString() {
    return String.format("LazyList<head=%s, tail=%s>", head, tail);
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
  public boolean retainAll(Collection<?> c) {
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
