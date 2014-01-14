/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package functional;

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
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assume.assumeNotNull;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 20, benchmarkRounds = 10)
@BenchmarkMethodChart(filePrefix = "filter-map-reduce")
@BenchmarkHistoryChart(filePrefix = "filter-map-reduce-history", labelWith = LabelType.TIMESTAMP)
public class FilterMapReduceTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("FilterMapReduce.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("FilterMapReduce.groovy");
  private static final Class<?> GroovyIndyClass = loadGroovyIndyClass("FilterMapReduce.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;
  private static final Var ClojureRunScript = clojureReference("filter-map-reduce.clj", "bench", "run");
  private static final Var ClojureLazyRunScript = clojureReference("filter-map-reduce.clj", "bench", "lazy");
  private static final ScriptEngine NashornEngine;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "filter-map-reduce.rb");
    NashornEngine = nashorn("filter-map-reduce.js");
  }

  private static final List<Integer> javaList;

  static {
    javaList = new LinkedList<>();
    for (int i = 0; i < 2_000_000; i++) {
      javaList.add(i % 500);
    }
  }

  @Test
  public void golo() throws Throwable {
    Object result = GoloModule.getMethod("run", Object.class).invoke(null, javaList);
  }

  @Test
  public void groovy() throws Throwable {
    Object result = GroovyClass.getMethod("run", Object.class).invoke(null, javaList);
  }

  @Test
  public void groovy_indy() throws Throwable {
    Object result = GroovyIndyClass.getMethod("run", Object.class).invoke(null, javaList);
  }

  @Test
  public void jruby() throws Throwable {
    JRubyContainer.put("@data_set", javaList);
    Object result = JRubyScript.run();
  }

  @Test
  public void clojure() throws Throwable {
    Object result = ClojureRunScript.invoke(javaList);
  }

  @Test
  public void clojure_lazy() throws Throwable {
    Object result = ClojureLazyRunScript.invoke();
  }

  @Test
  public void nashorn() throws Throwable {
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("run");
  }
}
