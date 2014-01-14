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

package closure;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.insalyon.citi.golo.benchmarks.GoloBenchmark;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Assume;
import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import static org.junit.Assume.assumeNotNull;

@BenchmarkOptions(clock = Clock.NANO_TIME, warmupRounds = 10, benchmarkRounds = 5)
@BenchmarkMethodChart(filePrefix = "call-closure")
@BenchmarkHistoryChart(filePrefix = "call-closure-history", labelWith = LabelType.TIMESTAMP)
public class CallClosureTest extends GoloBenchmark {

  private static final Class<?> GoloModule = loadGoloModule("CallClosure.golo");
  private static final Class<?> GroovyClass = loadGroovyClass("CallClosure.groovy");
  private static final Class<?> GroovyIndyClass = loadGroovyIndyClass("CallClosure.groovy");
  private static final ScriptingContainer JRubyContainer;
  private static final EmbedEvalUnit JRubyScript;
  private static final ScriptEngine NashornEngine;

  static {
    JRubyContainer = new ScriptingContainer();
    JRubyScript = jrubyEvalUnit(JRubyContainer, "callclosure.rb");
    NashornEngine = nashorn("callclosure.js");
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
