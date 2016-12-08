/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.eclipse.golo.compiler.GoloCompilationException;
import org.testng.TestNGException;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;

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

    assertThat(result, instanceOf(FunctionReference.class));

    FunctionReference func = (FunctionReference) result;
    assertThat((Integer) func.invoke(), is(3));
  }

  @Test
  public void function_with_arguments() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.asFunction("return a + b", "a", "b");

    assertThat(result, instanceOf(FunctionReference.class));
    FunctionReference func = (FunctionReference) result;

    assertThat(func.arity(), is(2));
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
    assertThat(result, instanceOf(FunctionReference.class));
    FunctionReference func = (FunctionReference) result;
    assertThat(func.arity(), is(2));
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
