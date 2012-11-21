package dispatch;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.citilab.gololang.benchmarks.GoloBenchmark;
import org.junit.Test;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 10)
@BenchmarkMethodChart(filePrefix = "polymorphic-dispatch")
@BenchmarkHistoryChart(filePrefix = "polymorphic-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class PolymorphicTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("Polymorphic.golo");

  @Test
  public void java() {
    Polymorphic.run();
  }

  @Test
  public void golo() throws Throwable {
    GoloModule.getMethod("run").invoke(null);
  }
}
