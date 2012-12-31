class CallClosure {
    static def run() {
        final def runner = { value -> "[" + value.toString() + "]" }
        def result = null
        for (def i = 0; i < 2000000; i = i + 1) {
            result = runner(i)
        }
        return ">>> " + result
    }
}
