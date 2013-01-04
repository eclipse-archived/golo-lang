package gololang;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IntRangeTest {

  private IntRange range;

  @BeforeMethod
  public void prepare() {
    range = new IntRange(1, 3);
  }

  @Test
  public void check() {
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(2));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void overflow() {
    Iterator<Integer> iterator = range.iterator();
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
    range = new IntRange(5, 4);
    assertThat(range.iterator().hasNext(), is(false));
  }

  @Test
  public void increment() {
    range.incrementBy(2);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(1));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void singleton() {
    range = new IntRange(0, 1);
    Iterator<Integer> iterator = range.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(0));
    assertThat(iterator.hasNext(), is(false));
  }
}
