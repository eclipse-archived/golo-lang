package gololang;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wraps a {@code Headtail} into an iterator
 */
public class HeadTailIterator<T> implements Iterator<T> {
  private HeadTail<T> data;

  HeadTailIterator(HeadTail<T> headTail) {
    this.data = headTail;
  }

  @Override
  public boolean hasNext() {
    return !data.isEmpty();
  }

  @Override
  public T next() {
    T h = data.head();
    data = data.tail();
    return h;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("HeadTail object are immutable");
  }
}
