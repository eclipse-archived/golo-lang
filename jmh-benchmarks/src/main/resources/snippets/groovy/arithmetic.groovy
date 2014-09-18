class Arithmetic {

    static def gcd(x, y, repeat) {
        def res = 0
        for (def i = 0; i < repeat; i++) {
            def a = x
            def b = y
            while (a != b) {
                if (a > b) {
                    a = a - b
                } else {
                    b = b - a
                }
            }
            res = a
        }
        return res
    }

    static long fast_gcd(long x, long y, long repeat) {
        long res = 0
        for (int i = 0; i < repeat; i++) {
            long a = x
            long b = y
            while (a != b) {
                if (a > b) {
                    a = a - b
                } else {
                    b = b - a
                }
            }
            res = a
        }
        return res
    }

    @groovy.transform.CompileStatic
    static long fastest_gcd(long x, long y, long repeat) {
        long res = 0
        for (int i = 0; i < repeat; i++) {
            long a = x
            long b = y
            while (a != b) {
                if (a > b) {
                    a = a - b
                } else {
                    b = b - a
                }
            }
            res = a
        }
        return res
    }

    static def sum(x, y) {
        return x + y
    }

    static long fast_sum(long x, long y) {
        return x + y
    }

    @groovy.transform.CompileStatic
    static long fastest_sum(long x, long y) {
        return x + y
    }
}
