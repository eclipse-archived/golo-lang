package closure;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.insalyon.citi.golo.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 10, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "call-closure")
@BenchmarkHistoryChart(filePrefix = "call-closure-history", labelWith = LabelType.TIMESTAMP)
public class CallClosureTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("CallClosure.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("CallClosure.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "callclosure.rb");
  }

  @Test
  public void java_boxed() {
    CallClosure.run_boxed();
  }

  @Test
  public void java_unboxed() {
    CallClosure.run_unboxed();
  }

  @Test
  public void golo() throws Throwable {
    GoloModule.getMethod("run").invoke(null);
  }

  @Test
  public void groovy() throws Throwable {
    GroovyClass.getMethod("run").invoke(null);
  }

  @Test
  public void jruby() throws Throwable {
    JRubyScript.run();
  }
}
