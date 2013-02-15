class TriMorphic {

    static Object run() {
        Object[] data = [
                "foo",
                666,
                "bar",
                999,
                "plop",
                "da",
                "plop",
                "for",
                "ever",
                1,
                2,
                3,
                4,
                5,
                6,
                new Object(),
                new Object(),
                new Object(),
                new Object()
        ]
        Object result = null
        for (int i = 0; i < 200000; i++) {
            for (int j = 0; j < data.length; j++) {
                result = data[j].toString()
            }
        }
        return result
    }
}