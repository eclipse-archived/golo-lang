package org.gololang.microbenchmarks.fibonacci;

public class JavaRecursiveFibonacci {

  public static long withPrimitives(long n) {
    if (n <= 2L) {
      return 1L;
    } else {
      return withPrimitives(n - 1L) + withPrimitives(n - 2L);
    }
  }

  // Note: the explicit boxing is voluntary
  public static Long withBoxing(Long n) {
    if (n.longValue() <= Long.valueOf(2L)) {
      return Long.valueOf(1L);
    } else {
      return Long.valueOf(withBoxing(Long.valueOf(n.longValue() - Long.valueOf(1L))) + withBoxing(Long.valueOf(n.longValue() - Long.valueOf(2L))));
    }
  }
}
