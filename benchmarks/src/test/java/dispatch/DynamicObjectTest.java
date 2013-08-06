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

import javax.script.Invocable;
import javax.script.ScriptEngine;

import static org.junit.Assume.assumeNotNull;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 20, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "dynamicobject-dispatch")
@BenchmarkHistoryChart(filePrefix = "dynamicobject-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class DynamicObjectTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("DynamicObjects.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("DynamicObjects.groovy");
  private static final Class<?> GroovyIndyClass = loadGroovyIndyClass("DynamicObjects.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;
  private static final ScriptEngine NashornEngine;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "dynamicobjects.rb");
    NashornEngine = nashorn("dynamicobjects.js");
  }

  @Test
  public void java() throws Throwable {
    LikeDynamicObject.run();
  }

  @Test
  public void java_boxing() throws Throwable {
    LikeDynamicObject.run_boxedloop();
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
  public void groovy_indy() throws Throwable {
    GroovyIndyClass.getMethod("run").invoke(null);
  }

  @Test
  public void jruby() throws Throwable {
    JRubyScript.run();
  }

  @Test
  public void nashorn() throws Throwable {
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("run");
  }
}
