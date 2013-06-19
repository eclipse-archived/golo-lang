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

package gololang;

import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import org.testng.TestNGException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class EvaluationEnvironmentTest {

  private static final String SIMPLE_MODULE =
      "module Foo\n\n" +
          "function plop = -> \"Plop!\"\n\n";

  private static final String SIMPLE_ANONYMOUS_MODULE = "function plop = -> \"Plop!\"\n\n";

  @Test
  public void module() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Class<?> module = (Class<?>) env.asModule(SIMPLE_MODULE);

    assertThat(module, not(nullValue()));
    assertThat((String) module.getMethod("plop").invoke(null), is("Plop!"));
  }

  @Test
  public void anonymous_module() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Class<?> module = (Class<?>) env.anonymousModule(SIMPLE_ANONYMOUS_MODULE);

    assertThat(module, not(nullValue()));
    assertThat((String) module.getMethod("plop").invoke(null), is("Plop!"));
  }

  @Test
  public void function() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.asFunction("let x = 1\nreturn x + 2");

    assertThat(result, instanceOf(MethodHandle.class));

    MethodHandle func = (MethodHandle) result;
    assertThat((Integer) func.invoke(), is(3));
  }

  @Test
  public void function_with_arguments() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.asFunction("return a + b", "a", "b");

    assertThat(result, instanceOf(MethodHandle.class));
    MethodHandle func = (MethodHandle) result;

    assertThat(func.type().parameterCount(), is(2));
    assertThat((Integer) func.invoke(10, 5), is(15));
  }

  @Test
  public void run() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.run("let x = 1\nreturn x + 2");

    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(3));
  }

  @Test
  public void run_with_context() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.run("return a + b", new HashMap<String, Object>() {
      {
        put("a", 11);
        put("b", 9);
      }
    });

    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(20));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void run_error() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    env.run("returnz false");
  }

  @Test
  public void run_with_imports() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment().imports("java.lang.Math");
    String snippet = "return max(1, 2)";

    Object result = env.run(snippet);
    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(2));

    env.clearImports();
    try {
      env.run(snippet);
      throw new TestNGException("A RuntimeException should have been raised");
    } catch (RuntimeException ignored) {
    }
  }

  @Test
  public void defunc() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    String code = "|a, b| -> a + b";

    Object result = env.def(code);
    assertThat(result, instanceOf(MethodHandle.class));
    MethodHandle func = (MethodHandle) result;
    assertThat(func.type().parameterCount(), is(2));
    assertThat((Integer) func.invoke(10, 5), is(15));
  }

  @Test
  public void check_source_present_with_compilation_error() {
    EvaluationEnvironment env = new EvaluationEnvironment();
    try {
      env.def("boom");
      throw new TestNGException("A GoloCompilationException should have been raised");
    } catch (GoloCompilationException e) {
      assertThat(e.getSourceCode(), notNullValue());
      assertThat(e.getSourceCode(), both(containsString("boom")).and(containsString("module anonymous")));
    }
  }
}
