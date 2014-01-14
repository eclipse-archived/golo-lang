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

package fr.insalyon.citi.golo.benchmarks;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.junit.Assume;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assume.assumeThat;

public class GoloBenchmark extends AbstractBenchmark {

  public static final String GOLO_SRC_DIR = "src/main/golo/";
  public static final String GROOVY_SRC_DIR = "src/main/groovy/";
  public static final String CLOJURE_SRC_DIR = "src/main/clojure/";
  public static final String RUBY_SRC_DIR = "src/main/ruby/";
  public static final String JS_SRC_DIR = "src/main/js/";

  private static GoloClassLoader goloClassLoader;
  private static GroovyClassLoader groovyClassLoader;
  private static GroovyClassLoader groovyIndyClassLoader;

  public void assumeSlowTests() {
    assumeThat(System.getProperty("slowTests"), not("false"));
  }

  public static GoloClassLoader goloClassLoader() {
    if (goloClassLoader == null) {
      goloClassLoader = new GoloClassLoader();
    }
    return goloClassLoader;
  }

  public static GroovyClassLoader groovyClassLoader() {
    if (groovyClassLoader == null) {
      groovyClassLoader = new GroovyClassLoader(GoloBenchmark.class.getClassLoader());
    }
    return groovyClassLoader;
  }

  public static GroovyClassLoader groovyIndyClassLoader() {
    if (groovyIndyClassLoader == null) {
      CompilerConfiguration.DEFAULT.getOptimizationOptions().put("indy", true);
      CompilerConfiguration configuration = new CompilerConfiguration();
      configuration.getOptimizationOptions().put("indy", true);
      groovyClassLoader = new GroovyClassLoader(GoloBenchmark.class.getClassLoader(), configuration);
    }
    return groovyClassLoader;
  }

  public static Class<?> loadGoloModule(String goloSourceFilename) {
    try (FileInputStream in = new FileInputStream(GOLO_SRC_DIR + goloSourceFilename)) {
      return goloClassLoader().load(goloSourceFilename, in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Class<?> loadGroovyClass(String groovySourceFilename) {
    try {
      return groovyClassLoader().parseClass(new File(GROOVY_SRC_DIR + groovySourceFilename));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Class<?> loadGroovyIndyClass(String groovySourceFilename) {
    try {
      return groovyIndyClassLoader().parseClass(new File(GROOVY_SRC_DIR + groovySourceFilename));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static clojure.lang.Var clojureReference(String clojureSourceFilename, String namespace, String referenceName) {
    try {
      // Damn you Clojure 1.5, somehow RT needs to be loaded in a way or the other
      Class.forName("clojure.lang.RT");
      clojure.lang.Compiler.loadFile(CLOJURE_SRC_DIR + clojureSourceFilename);
      return clojure.lang.RT.var(namespace, referenceName);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static EmbedEvalUnit jrubyEvalUnit(ScriptingContainer scriptingContainer, String rubySourceFilename) {
    try (InputStream in = new FileInputStream(RUBY_SRC_DIR + rubySourceFilename)) {
      return scriptingContainer.parse(in, rubySourceFilename);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static ScriptEngine nashorn(String jsFile) {
    try {
      ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
      if (engine != null) {
        engine.eval(new FileReader(JS_SRC_DIR + jsFile));
      }
      return engine;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
