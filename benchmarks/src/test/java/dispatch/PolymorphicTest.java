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

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 10)
@BenchmarkMethodChart(filePrefix = "polymorphic-dispatch")
@BenchmarkHistoryChart(filePrefix = "polymorphic-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class PolymorphicTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("Polymorphic.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("Polymorphic.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "polymorphic.rb");
  }

  @Test
  public void java() {
    Polymorphic.run();
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
