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

import javax.script.Invocable;
import javax.script.ScriptEngine;

import static org.jruby.javasupport.JavaEmbedUtils.javaToRuby;
import static org.junit.Assume.assumeNotNull;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 15, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "fibonacci")
@BenchmarkHistoryChart(filePrefix = "fibonacci-history", labelWith = LabelType.TIMESTAMP)
public class FibonacciTest extends GoloBenchmark {

  private static final Class<?> FibonacciModule = loadGoloModule("Fibonacci.golo");
  private static final Class<?> GroovyFibonacci = loadGroovyClass("GroovyFibonacci.groovy");
  private static final Class<?> GroovyIndyFibonacci = loadGroovyIndyClass("GroovyFibonacci.groovy");
  private static final Var ClojureFibonacci = clojureReference("fibonacci.clj", "fibonacci", "fib");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyFibonacci;
  private static final ScriptEngine NashornEngine;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyFibonacci = jrubyEvalUnit(JRubyContainer, "fibonacci.rb");
    NashornEngine = nashorn("fibonacci.js");
  }

  private void goloFibonacci(int n) throws Throwable {
    FibonacciModule.getMethod("fib", Object.class).invoke(null, n);
  }

  private void groovyFibonacci(int n) throws Throwable {
    GroovyFibonacci.getMethod("fib", Object.class).invoke(null, n);
  }

  private void groovyIndyFibonacci(int n) throws Throwable {
    GroovyIndyFibonacci.getMethod("fib", Object.class).invoke(null, n);
  }

  @Test
  public void java_boxing_30() {
    Fibonacci.fib_boxed(30);
  }

  @Test
  public void java_boxing_38() {
    Fibonacci.fib_boxed(38);
  }

  @Test
  public void java_boxing_40() {
    Fibonacci.fib_boxed(40);
  }

  @Test
  public void java_unboxed_30() {
    Fibonacci.fib_unboxed(30);
  }

  @Test
  public void java_unboxed_38() {
    Fibonacci.fib_unboxed(38);
  }

  @Test
  public void java_unboxed_40() {
    Fibonacci.fib_unboxed(40);
  }

  @Test
  public void java_reflective_30() throws Throwable {
    Fibonacci.fib_reflective(30);
  }

  @Test
  public void java_reflective_38() throws Throwable {
    Fibonacci.fib_reflective(38);
  }

  @Test
  public void java_reflective_40() throws Throwable {
    Fibonacci.fib_reflective(40);
  }

  @Test
  public void golo_30() throws Throwable {
    goloFibonacci(30);
  }

  @Test
  public void golo_38() throws Throwable {
    goloFibonacci(38);
  }

  @Test
  public void golo_40() throws Throwable {
    goloFibonacci(40);
  }

  @Test
  public void groovy_30() throws Throwable {
    groovyFibonacci(30);
  }

  @Test
  public void groovy_38() throws Throwable {
    groovyFibonacci(38);
  }

  @Test
  public void groovy_40() throws Throwable {
    groovyFibonacci(40);
  }

  @Test
  public void groovy_indy_30() throws Throwable {
    groovyIndyFibonacci(30);
  }

  @Test
  public void groovy_indy_38() throws Throwable {
    groovyIndyFibonacci(38);
  }

  @Test
  public void groovy_indy_40() throws Throwable {
    groovyIndyFibonacci(40);
  }

  @Test
  public void clojure_30() throws Throwable {
    ClojureFibonacci.invoke(30);
  }

  @Test
  public void clojure_38() throws Throwable {
    ClojureFibonacci.invoke(38);
  }

  @Test
  public void clojure_40() throws Throwable {
    ClojureFibonacci.invoke(40);
  }

  @Test
  public void jruby_30() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 30));
    JRubyFibonacci.run();
  }

  @Test
  public void jruby_38() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 38));
    JRubyFibonacci.run();
  }

  @Test
  public void jruby_40() throws Throwable {
    JRubyContainer.put("@goal", javaToRuby(JRubyContainer.getProvider().getRuntime(), 40));
    JRubyFibonacci.run();
  }

  @Test
  public void nashorn_30() throws Throwable {
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("fib", 30);
  }

  @Test
  public void nashorn_38() throws Throwable {
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("fib", 38);
  }

  @Test
  public void nashorn_40() throws Throwable {
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("fib", 40);
  }
}
