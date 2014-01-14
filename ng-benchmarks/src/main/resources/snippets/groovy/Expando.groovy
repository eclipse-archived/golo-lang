class ExpandoDynamicObject {

    static class Plop {

        def random = new java.util.Random()

        def plop() {
            return random.nextInt()
        }
    }

    static def provide() {
        def plop = new Expando()
        plop.random = new java.util.Random()
        plop.plop = {
            return plop.random.nextInt()
        }
        return plop
    }

    static def provide_concrete_class() {
        return new Plop()
    }

    static def dispatch(def plop) {
        return plop.plop()
    }
}