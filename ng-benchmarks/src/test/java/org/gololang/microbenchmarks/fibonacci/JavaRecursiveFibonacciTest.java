package org.gololang.microbenchmarks.fibonacci;

import org.junit.Test;

import static org.gololang.microbenchmarks.fibonacci.JavaRecursiveFibonacci.withBoxing;
import static org.gololang.microbenchmarks.fibonacci.JavaRecursiveFibonacci.withPrimitives;
import static org.junit.Assert.assertEquals;

public class JavaRecursiveFibonacciTest {

  @Test
  public void test_withPrimitives() throws Exception {
    assertEquals(1L, withPrimitives(1L));
    assertEquals(1L, withPrimitives(2L));
    assertEquals(2L, withPrimitives(3L));
    assertEquals(3L, withPrimitives(4L));
    assertEquals(5L, withPrimitives(5L));
    assertEquals(8L, withPrimitives(6L));
    assertEquals(13L, withPrimitives(7L));
  }

  @Test
  public void test_withBoxing() throws Exception {
    assertEquals((Long) 1L, withBoxing(1L));
    assertEquals((Long) 1L, withBoxing(2L));
    assertEquals((Long) 2L, withBoxing(3L));
    assertEquals((Long) 3L, withBoxing(4L));
    assertEquals((Long) 5L, withBoxing(5L));
    assertEquals((Long) 8L, withBoxing(6L));
    assertEquals((Long) 13L, withBoxing(7L));
  }
}
