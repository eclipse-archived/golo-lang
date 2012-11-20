class Monomorphic {

    static def run() {
        def i = 0
        def max = 5000000
        def result = null
        while (i.intValue() < max.intValue()) {
            result = i.toString()
            i = i.intValue() + 1
        }
        return result
    }
}
