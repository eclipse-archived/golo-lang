package gololang;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LongRangeTest {

  private LongRange range;

  @BeforeMethod
  public void prepare() {
    range = new LongRange(1, 3);
  }

  @Test
  public void check() {
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1L));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(2L));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3L));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    Iterator<Long> iterator = range.iterator();
    for (int i = 0; i < 4; i++) {
      iterator.next();
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void remove() {
    range.iterator().remove();
  }

  @Test
  public void empty() {
    range = new LongRange(5, 4);
    assertThat(range.iterator().hasNext(), is(false));
  }

  @Test
  void increment() {
    range.incrementBy(2);
    Iterator<Long> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1L));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(3L));
    assertThat(iterator.hasNext(), is(false));
  }
}
