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

package compiler;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import fr.insalyon.citi.golo.benchmarks.GoloBenchmark;
import fr.insalyon.citi.golo.compiler.CodeGenerationResult;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import groovy.lang.GroovyClassLoader;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.List;

/*
 * This benchmark measures the perceived time to parse, compile and emit bytecode.
 *
 * Relative numbers do not mean much in this benchmark. We make comparisons over Golo, Groovy and JRuby has they
 * have been used in a similar number of benchmarks, hence we have roughly the same type of files. Nevertheless, this
 * remains a kind of "oranges and apple" comparison, so drawing conclusions from here is anything but a sound idea.
 *
 * What's more interesting is to track how Golo times evolve over time.
 */

@BenchmarkOptions(clock = Clock.NANO_TIME)
@BenchmarkMethodChart(filePrefix = "compiler")
@BenchmarkHistoryChart(filePrefix = "compiler-history", labelWith = LabelType.TIMESTAMP)
public class CompilerTest extends GoloBenchmark {

  private final int ITERATIONS = 1000;

  @Test
  public void golo() throws Throwable {
    File[] files = new File(GOLO_SRC_DIR).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".golo");
      }
    });
    for (int i = 0; i < ITERATIONS; i++) {
      GoloCompiler compiler = new GoloCompiler();
      for (File file : files) {
        FileInputStream inputStream = new FileInputStream(file);
        List<CodeGenerationResult> results = compiler.compile(file.getName(), inputStream);
      }
    }
  }

  @Test
  public void groovy() throws Throwable {
    File[] files = new File(GROOVY_SRC_DIR).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".groovy");
      }
    });
    for (int i = 0; i < ITERATIONS; i++) {
      GroovyClassLoader loader = new GroovyClassLoader();
      for (File file : files) {
        Class<?> klass = loader.parseClass(file);
      }
    }
  }

  @Test
  public void jruby() throws Throwable {
    File[] files = new File(RUBY_SRC_DIR).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".rb");
      }
    });
    for (int i = 0; i < ITERATIONS; i++) {
      ScriptingContainer container = new ScriptingContainer();
      container.setCompileMode(RubyInstanceConfig.CompileMode.FORCE);
      for (File file : files) {
        EmbedEvalUnit script = jrubyEvalUnit(container, file.getName());
      }
    }
  }
}
