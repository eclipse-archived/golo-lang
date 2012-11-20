package dispatch;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.citilab.gololang.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

@BenchmarkOptions(clock = Clock.NANO_TIME)
@BenchmarkMethodChart(filePrefix = "monomorphic-dispatch")
@BenchmarkHistoryChart(filePrefix = "monomorphic-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class MonomorphicTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("Monomorphic.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("Monomorphic.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "monomorphic.rb");
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
  public void java() throws Throwable {
    Monomorphic.run();
  }

  @Test
  public void jruby() throws Throwable {
    JRubyScript.run();
  }
}
