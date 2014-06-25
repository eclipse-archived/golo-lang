class Arithmetic {

    static def gcd(x, y) {
        def a = x
        def b = y
        while (a != b) {
            if (a > b) {
                a = a - b
            } else {
                b = b - a
            }
        }
        return a
    }

    static int fast_gcd(int x, int y) {
        int a = x
        int b = y
        while (a != b) {
            if (a > b) {
                a = a - b
            } else {
                b = b - a
            }
        }
        return a
    }

    @groovy.transform.CompileStatic
    static int fastest_gcd(int x, int y) {
        int a = x
        int b = y
        while (a != b) {
            if (a > b) {
                a = a - b
            } else {
                b = b - a
            }
        }
        return a
    }
}

