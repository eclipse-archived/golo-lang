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

import org.testng.TestNGException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;

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
    Class<?> module = (Class<?>) env.module(SIMPLE_MODULE);

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
    Object result = env.function("let x = 1\nreturn x + 2");

    assertThat(result, instanceOf(MethodHandle.class));

    MethodHandle func = (MethodHandle) result;
    assertThat((Integer) func.invoke(), is(3));
  }

  @Test
  public void run() throws Throwable {
    EvaluationEnvironment env = new EvaluationEnvironment();
    Object result = env.run("let x = 1\nreturn x + 2");

    assertThat(result, instanceOf(Integer.class));
    assertThat((Integer) result, is(3));
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
}
