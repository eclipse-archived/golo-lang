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

package gololang;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * An evaluation environment offers facilities for dynamic code compilation, loading and execution from Golo code as
 * strings.
 * <p>
 * An evaluation environment is reusable across several executions. The only exception is when using {@code asModule()},
 * as attempts to load a module with the same name as an already loaded one fails.
 * <p>
 * Each instance of this class uses a dedicated {@link GoloClassLoader}, hence usual rules about classloader delegation
 * and isolation apply to evaluation environments.
 * <p>
 * While dynamic code evaluation is useful, it shall still be used with care and parsimony. It is especially important
 * not to abuse {@code run()}, as each invocation triggers the generation of a one-shot class.
 * <p>
 * Here is an example usage of this API:
 * <pre>
 * let env = EvaluationEnvironment()
 * let code =
 * """
 * function a = -> "a."
 * function b = -> "b."
 * """
 * let mod = env: anonymousModule(code)
 * let a = fun("a", mod)
 * let b = fun("b", mod)
 * println(a())
 * println(b())
 * </pre>
 * <p>
 * While this class is expected to be used from Golo code, it can also be used as a convenient way to embed Golo into
 * polyglot JVM applications.
 */
public class EvaluationEnvironment {

  private final GoloClassLoader goloClassLoader;
  private final List<String> imports = new LinkedList<>();

  private static String anonymousFilename() {
    return "$Anonymous$_" + System.nanoTime() + ".golo";
  }

  private static String anonymousModuleName() {
    return "module anonymous" + System.nanoTime();
  }

  /**
   * Creates an evaluation environment using the current thread context classloader.
   */
  public EvaluationEnvironment() {
    this(Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an evaluation environment using a parent classloader.
   *
   * @param parentClassLoader the parent classloader.
   */
  public EvaluationEnvironment(ClassLoader parentClassLoader) {
    goloClassLoader = new GoloClassLoader(parentClassLoader);
  }

  /**
   * Imports symbols.
   * <p>
   * Each symbol generates an equivalent {@code import} statement in the corresponding Golo code. Calling
   * {@code imports("foo.Bar", "bar.Baz")} means that the subsequent code evaluations have {@code import foo.Bar} and
   * {@code import bar.Baz} statements.
   * <p>
   * Note that this has no effect for {@link #asModule(String)}. Also, calling this method several times accumulates
   * the imports, in order.
   *
   * @param head the first imported symbol.
   * @param tail the next imported symbols.
   * @return this evaluation environment.
   */
  public EvaluationEnvironment imports(String head, String... tail) {
    imports.add(head);
    Collections.addAll(imports, tail);
    return this;
  }

  /**
   * Clears all import symbols for the next code evaluation requests.
   *
   * @return this evaluation environment.
   */
  public EvaluationEnvironment clearImports() {
    imports.clear();
    return this;
  }

  /**
   * Evaluates a complete module, as in:
   * <pre>
   * let code =
   * """
   * module foo
   *
   * function a = -> "a!"
   * function b = -> "b!"
   * """
   * let mod = env: asModule(code)
   * let a = fun("a", mod)
   * let b = fun("b", mod)
   * println(a())
   * println(b())
   * </pre>
   *
   * @param source the module Golo source code as a string.
   * @return the corresponding module, as a {@link Class}.
   * @see Predefined#fun(Object, Object)
   */
  public Object asModule(String source) {
    try (InputStream in = new ByteArrayInputStream(source.getBytes())) {
      return goloClassLoader.load(anonymousFilename(), in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (GoloCompilationException e) {
      e.setSourceCode(source);
      throw e;
    }
  }

  /**
   * Loads an anonymous module. This is the same as {@link #asModule(String)}, except that the code does not contain
   * a {@code module} declaration.
   *
   * <pre>
   * let code =
   * """
   * function a = -> "a!"
   * function b = -> "b!"
   * """
   * let mod = env: anonymousModule(code)
   * let a = fun("a", mod)
   * let b = fun("b", mod)
   * println(a())
   * println(b())
   * </pre>
   *
   * @param source the module Golo source code as a string.
   * @return the corresponding module, as a {@link Class}.
   * @see Predefined#fun(Object, Object)
   */
  public Object anonymousModule(String source) {
    return asModule(anonymousModuleName() + "\n\n" + source);
  }

  /**
   * Defines a function, and returns it.
   *
   * <pre>
   * let code = "|a, b| -> (a + b) * 2"
   * let f = env: def(code)
   * println(f(10, 20))
   * </pre>
   *
   * @param source the function code.
   * @return the function as a {@link java.lang.invoke.MethodHandle} instance.
   */
  public Object def(String source) {
    return loadAndRun("return " + source, "$_code");
  }

  /**
   * Evaluates some code as the body of a function and returns it.
   *
   * <pre>
   * let code = "return (a + b) * 2"
   * let f = env: asFunction(code, "a", "b")
   * println(f(10, 20))
   * </pre>
   *
   * @param source        the function body source code.
   * @param argumentNames the argument names.
   * @return the function as a {@link java.lang.invoke.MethodHandle} instance.
   */
  public Object asFunction(String source, String... argumentNames) {
    return loadAndRun(source, "$_code_ref", argumentNames);
  }

  /**
   * Runs some code as the body of a function and returns the value. The code shall use {@code return} statements
   * to provide return values, if any.
   *
   * <pre>
   * let code = """println(">>> run")
   * foreach (i in range(0, 3)) {
   *   println("w00t")
   * }
   * return 666"""
   * env: run(code)
   *
   * </pre>
   *
   * @param source the source to run.
   * @return the return value, or {@code null} if no {@code return} statement is used.
   */
  public Object run(String source) {
    return loadAndRun(source, "$_code");
  }

  /**
   * Runs some code as the body of a function and returns the value. This is the same as {@link #run(String)}, but it
   * takes a set of reference bindings in a map. Each reference is equivalent to a {@code let} statement.
   *
   * <pre>
   * let code = """println(">>> run_map")
   * println(a)
   * println(b)
   * """
   * let values = java.util.TreeMap(): add("a", 1): add("b", 2)
   * env: run(code, values)
   * </pre>
   *
   * @param source  the source to run.
   * @param context a map of bindings from name to values.
   * @return the return value, or {@code null} if no {@code return} statement is used.
   */
  public Object run(String source, Map<String, Object> context) {
    StringBuilder builder = new StringBuilder();
    for (String param : context.keySet()) {
      builder
          .append("let ")
          .append(param)
          .append(" = $_env: get(\"")
          .append(param)
          .append("\")\n");
    }
    builder.append(source);
    return loadAndRun(builder.toString(), "$_code", new String[]{"$_env"}, new Object[]{context});
  }

  private Class<?> wrapAndLoad(String source, String... argumentNames) {
    StringBuilder builder = new StringBuilder()
        .append(anonymousModuleName())
        .append("\n");
    for (String importSymbol : imports) {
      builder.append("import ").append(importSymbol).append("\n");
    }
    builder.append("\nfunction $_code = ");
    if (argumentNames.length > 0) {
      builder.append("| ");
      final int lastIndex = argumentNames.length - 1;
      for (int i = 0; i < argumentNames.length; i++) {
        builder.append(argumentNames[i]);
        if (i < lastIndex) {
          builder.append(", ");
        }
      }
      builder.append(" |");
    }
    builder
        .append(" {\n")
        .append(source)
        .append("\n}\n\n")
        .append("function $_code_ref = -> ^$_code\n\n");
    return (Class<?>) asModule(builder.toString());
  }

  private Object loadAndRun(String source, String target, String... argumentNames) {
    try {
      Class<?> module = wrapAndLoad(source, argumentNames);
      return module.getMethod(target).invoke(null);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Object loadAndRun(String source, String target, String[] argumentNames, Object[] arguments) {
    try {
      Class<?> module = wrapAndLoad(source, argumentNames);
      Class<?>[] type = new Class<?>[argumentNames.length];
      Arrays.fill(type, Object.class);
      return module.getMethod(target, type).invoke(null, arguments);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
