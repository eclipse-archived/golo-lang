package gololang.testing;

import gololang.Tuple;

@FunctionalInterface
public interface TestReporter {

  int report(Tuple results);
}
