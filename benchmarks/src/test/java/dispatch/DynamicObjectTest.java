package dispatch;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.insalyon.citi.golo.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 20, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "dynamicobject-dispatch")
@BenchmarkHistoryChart(filePrefix = "dynamicobject-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class DynamicObjectTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("DynamicObjects.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("DynamicObjects.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "dynamicobjects.rb");
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
