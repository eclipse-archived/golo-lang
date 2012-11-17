package fibonacci;

public class JavaFibonacciBoxingReference {

  public static Object fib(Object arg) {
    Integer n = (Integer) arg;
    if (n < 2) {
      return n;
    } else {
      return ((Integer) fib(n - 1)) + ((Integer) fib(n - 2));
    }
  }
}
