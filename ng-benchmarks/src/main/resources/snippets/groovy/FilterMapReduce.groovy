class FilterMapReduce {

    static def run(dataSet) {
        return dataSet
                .findAll { x -> x % 2L == 0L }
                .collect { x -> x * 2L }
                .inject(0L) { acc, x -> acc + x }
    }
}