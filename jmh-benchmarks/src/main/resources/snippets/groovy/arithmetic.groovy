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

    static int fast_gcd(int x, int y, int repeat) {
        int res = 0
        for (int i = 0; i < repeat; i++) {
            int a = x
            int b = y
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
    static int fastest_gcd(int x, int y, int repeat) {
        int res = 0
        for (int i = 0; i < repeat; i++) {
            int a = x
            int b = y
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

    static int fast_sum(int x, int y) {
        return x + y
    }

    @groovy.transform.CompileStatic
    static int fastest_sum(int x, int y) {
        return x + y
    }
}
