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

package org.gololang.microbenchmarks.support;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jruby.embed.ScriptingContainer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CodeLoader {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public MethodHandle golo(String file, String func, int argCount) {
    GoloClassLoader classLoader = new GoloClassLoader();
    String filename = "snippets/golo/" + file + ".golo";
    Class<?> module = classLoader.load(filename, CodeLoader.class.getResourceAsStream("/" + filename));
    try {
      return LOOKUP.findStatic(module, func, MethodType.genericMethodType(argCount));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public MethodHandle groovy(String file, String method, MethodType type) {
    CompilerConfiguration.DEFAULT.getOptimizationOptions().put("indy", false);
    CompilerConfiguration configuration = new CompilerConfiguration();
    configuration.getOptimizationOptions().put("indy", false);
    GroovyClassLoader classLoader = new GroovyClassLoader();
    return _groovy(file, method, type, classLoader);
  }

  public MethodHandle groovy_indy(String file, String method, MethodType type) {
    CompilerConfiguration.DEFAULT.getOptimizationOptions().put("indy", true);
    CompilerConfiguration configuration = new CompilerConfiguration();
    configuration.getOptimizationOptions().put("indy", true);
    GroovyClassLoader classLoader = new GroovyClassLoader();
    return _groovy(file, method, type, classLoader);
  }

  private MethodHandle _groovy(String file, String method, MethodType type, GroovyClassLoader classLoader) {
    String filename = "snippets/groovy/" + file + ".groovy";
    Class klass = classLoader.parseClass(CodeLoader.class.getResourceAsStream("/" + filename), filename);
    try {
      return LOOKUP.findStatic(klass, method, type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public clojure.lang.Var clojure(String file, String namespace, String ref) {
    try {
      // Damn you Clojure 1.5, somehow RT needs to be loaded in a way or the other
      Class.forName("clojure.lang.RT");
      String filename = "snippets/clojure/" + file + ".clj";
      InputStreamReader reader = new InputStreamReader(CodeLoader.class.getResourceAsStream("/" + filename));
      clojure.lang.Compiler.load(reader);
      return clojure.lang.RT.var(namespace, ref);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public JRubyContainerAndReceiver jruby(String file) {
    ScriptingContainer container = new ScriptingContainer();
    String filename = "snippets/jruby/" + file + ".rb";
    return new JRubyContainerAndReceiver(container,
        container.runScriptlet(CodeLoader.class.getResourceAsStream("/" + filename), filename));
  }

  public ScriptEngine nashorn(String file) {
    try {
      ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
      if (engine != null) {
        engine.eval(new InputStreamReader(CodeLoader.class.getResourceAsStream("/snippets/js/" + file + ".js")));
      }
      return engine;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
