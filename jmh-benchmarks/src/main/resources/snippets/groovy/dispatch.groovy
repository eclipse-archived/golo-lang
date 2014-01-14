class Dispatch {

    static Object dispatch(Object[] data) {
        String result = ""
        for (Object obj : data) {
            result = obj.toString()
        }
        return result
    }
}