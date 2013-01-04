package gololang;

import java.util.Iterator;
import java.util.NoSuchElementException;

class LongRange implements Iterable<Long> {

  private final long from;
  private final long to;
  private long increment = 1;

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
