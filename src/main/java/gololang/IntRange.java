package gololang;

import java.util.Iterator;
import java.util.NoSuchElementException;

class IntRange implements Iterable<Integer> {
  private final int from;
  private final int to;
  private int increment = 1;

  public IntRange(int from, int to) {
    this.from = from;
    this.to = to;
  }

  public IntRange incrementBy(int value) {
    this.increment = value;
    return this;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {

      private boolean started = false;
      private int current = from;

      @Override
      public boolean hasNext() {
        return current < to;
      }

      @Override
      public Integer next() {
        int value = current;
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
