package closure;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.citilab.gololang.benchmarks.GoloBenchmark;
import org.junit.Test;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 10)
@BenchmarkMethodChart(filePrefix = "call-closure")
@BenchmarkHistoryChart(filePrefix = "call-closure", labelWith = LabelType.TIMESTAMP)
public class CallClosureTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("CallClosure.golo");

  @Test
  public void golo() throws Throwable {
    GoloModule.getMethod("run").invoke(null);
  }
}
