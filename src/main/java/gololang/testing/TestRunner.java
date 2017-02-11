package gololang.testing;

import gololang.Tuple;

@FunctionalInterface
public interface TestRunner {

  Tuple run(Tuple suites);
}
