/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fibonacci;

import clojure.lang.Var;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.insalyon.citi.golo.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

import static org.jruby.javasupport.JavaEmbedUtils.javaToRuby;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 15, benchmarkRounds = 5)
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
    Fibonacci.fib_boxed(30);
  }

  @Test
  public void java_object_boxing_reference_38() {
    Fibonacci.fib_boxed(38);
  }

  @Test
  public void java_object_boxing_reference_40() {
    Fibonacci.fib_boxed(40);
  }

  @Test
  public void java_unboxed_reference_30() {
    Fibonacci.fib_unboxed(30);
  }

  @Test
  public void java_unboxed_reference_38() {
    Fibonacci.fib_unboxed(38);
  }

  @Test
  public void java_unboxed_reference_40() {
    Fibonacci.fib_unboxed(40);
  }

  @Test
  public void golo_fibonacci_30() throws Throwable {
    goloFibonacci(30);
  }

  @Test
  public void golo_fibonacci_38() throws Throwable {
    goloFibonacci(38);
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
  public void groovy_fibonacci_38() throws Throwable {
    groovyFibonacci(38);
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
  public void clojure_fibonacci_38() throws Throwable {
    ClojureFibonacci.invoke(38);
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
  public void jruby_fibonacci_38() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 38));
    JRubyFibonacci.run();
  }

  @Test
  public void jruby_fibonacci_40() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 40));
    JRubyFibonacci.run();
  }
}
