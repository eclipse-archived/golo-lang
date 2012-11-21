package fibonacci;

public class Fibonacci {

  public static int fib_unboxed(int n) {
    if (n < 2) {
      return n;
    } else {
      return fib_unboxed(n - 1) + fib_unboxed(n - 2);
    }
  }

  public static Object fib_boxed(Object arg) {
    Integer n = (Integer) arg;
    if (n < 2) {
      return n;
    } else {
      return ((Integer) fib_boxed(n - 1)) + ((Integer) fib_boxed(n - 2));
    }
  }
}
