package fr.citilab.gololang.benchmarks;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import fr.insalyon.citi.golo.runtime.GoloClassLoader;
import groovy.lang.GroovyClassLoader;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;

import java.io.*;

public class GoloBenchmark extends AbstractBenchmark {

  private static String GOLO_SRC_DIR = "src/main/golo/";
  private static String GROOVY_SRC_DIR = "src/main/groovy/";
  private static String CLOJURE_SRC_DIR = "src/main/clojure/";
  private static String RUBY_SRC_DIR = "src/main/ruby/";

  private static GoloClassLoader goloClassLoader;
  private static GroovyClassLoader groovyClassLoader;

  public static GoloClassLoader goloClassLoader() {
    if (goloClassLoader == null) {
      goloClassLoader = new GoloClassLoader();
    }
    return goloClassLoader;
  }

  public static GroovyClassLoader groovyClassLoader() {
    if (groovyClassLoader == null) {
      groovyClassLoader = new GroovyClassLoader();
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

  public static clojure.lang.Var clojureReference(String clojureSourceFilename, String namespace, String referenceName) {
    try {
      clojure.lang.Compiler.loadFile(CLOJURE_SRC_DIR + clojureSourceFilename);
      return clojure.lang.RT.var(namespace, referenceName);
    } catch (IOException e) {
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
}
