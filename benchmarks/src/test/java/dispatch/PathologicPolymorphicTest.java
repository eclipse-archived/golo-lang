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

package dispatch;

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

import static org.junit.Assume.assumeNotNull;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 15, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "pathologic-polymorphic-dispatch")
@BenchmarkHistoryChart(filePrefix = "pathologic-polymorphic-dispatch-history", labelWith = LabelType.TIMESTAMP)
public class PathologicPolymorphicTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("PathologicPolymorphic.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("PathologicPolymorphic.groovy");
  private static final Class<?> GroovyIndyClass = loadGroovyIndyClass("PathologicPolymorphic.groovy");
  private static final Var ClojureRun = clojureReference("pathologic-polymorphic.clj", "megamorphic", "run");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;
  private static final ScriptEngine NashornEngine;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "pathologic-polymorphic.rb");
    NashornEngine = nashorn("pathologic-polymorphic.js");
  }

  @Test
  public void java() {
    PathologicPolymorphic.run();
  }

  @Test
  public void java_reflective_object() throws Throwable {
    PathologicPolymorphic.run_reflective_object();
  }

  @Test
  public void java_reflective_pic() throws Throwable {
    PathologicPolymorphic.run_reflective_pic();
  }

  @Test
  public void golo() throws Throwable {
    GoloModule.getMethod("run").invoke(null);
  }

  @Test
  public void golo_elvis_nulls() throws Throwable {
    GoloModule.getMethod("run_with_elvis_and_nulls").invoke(null);
  }

  @Test
  public void groovy() throws Throwable {
    GroovyClass.getMethod("run").invoke(null);
  }

  @Test
  @BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 1)
  public void groovy_indy() throws Throwable {
    assumeSlowTests();
    GroovyIndyClass.getMethod("run").invoke(null);
  }

  @Test
  public void jruby() throws Throwable {
    JRubyScript.run();
  }

  @Test
  @BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 1)
  public void nashorn() throws Throwable {
    assumeSlowTests();
    assumeNotNull(NashornEngine);
    Invocable invocable = (Invocable) NashornEngine;
    invocable.invokeFunction("run");
  }

  @Test
  public void clojure() {
    ClojureRun.invoke();
  }
}
