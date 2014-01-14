class Fibonacci {

    static def fib(n) {
        if (n <= 2L) {
            return 1L;
        } else {
            return fib(n - 1L) + fib(n - 2L);
        }
    }
}