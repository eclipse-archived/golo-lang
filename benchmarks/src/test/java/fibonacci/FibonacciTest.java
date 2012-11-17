package fibonacci;

import clojure.lang.Var;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.citilab.gololang.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

import static org.jruby.javasupport.JavaEmbedUtils.javaToRuby;

@BenchmarkMethodChart(filePrefix = "fibonacci")
@BenchmarkHistoryChart(filePrefix = "fibonacci-history", labelWith = LabelType.TIMESTAMP)

public class FibonacciTest extends GoloBenchmark {

  private static final Class<?> FibonacciModule = loadGoloModule("Fibonacci.golo");
  private static final Class<?> GroovyFibonacci = loadGroovyClass("GroovyFibonacci.groovy");
  private static final Var ClojureFibonacci = clojureReference("fibonacci.clj", "fibonacci", "fib");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyFibonacci;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyFibonacci = jrubyEvalUnit(JRubyContainer, "fibonacci.rb");
  }

  private void goloFibonacci(int n) throws Throwable {
    FibonacciModule.getMethod("fib", Object.class).invoke(null, n);
  }

  private void groovyFibonacci(int n) throws Throwable {
    GroovyFibonacci.getMethod("fib", Object.class).invoke(null, n);
  }

  @Test
  public void java_object_boxing_reference_30() {
    JavaFibonacciBoxingReference.fib(30);
  }

  @Test
  public void java_object_boxing_reference_40() {
    JavaFibonacciBoxingReference.fib(40);
  }

  @Test
  public void golo_fibonacci_30() throws Throwable {
    goloFibonacci(30);
  }

  @Test
  public void golo_fibonacci_40() throws Throwable {
    goloFibonacci(40);
  }

  @Test
  public void groovy_fibonacci_30() throws Throwable {
    groovyFibonacci(30);
  }

  @Test
  public void groovy_fibonacci_40() throws Throwable {
    groovyFibonacci(40);
  }

  @Test
  public void clojure_fibonacci_30() throws Throwable {
    ClojureFibonacci.invoke(30);
  }

  @Test
  public void clojure_fibonacci_40() throws Throwable {
    ClojureFibonacci.invoke(40);
  }

  @Test
  public void jruby_fibonacci_30() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 30));
    JRubyFibonacci.run();
  }

  @Test
  public void jruby_fibonacci_40() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 40));
    JRubyFibonacci.run();
  }
}
