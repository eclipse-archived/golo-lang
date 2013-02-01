class FilterMapReduce {

    static def run(dataSet) {
        return dataSet
            .collect { x -> x + 2 }
            .findAll { x -> x % 2 == 0 }
            .inject(0) { acc, x -> acc + x }
    }
}
