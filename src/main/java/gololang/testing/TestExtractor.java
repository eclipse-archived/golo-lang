package gololang.testing;

import gololang.Tuple;

import java.nio.file.Path;

@FunctionalInterface
public interface TestExtractor {

  Tuple extract(Path path);
}
