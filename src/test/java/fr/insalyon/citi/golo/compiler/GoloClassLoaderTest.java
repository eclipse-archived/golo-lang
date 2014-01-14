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

package fr.insalyon.citi.golo.compiler;

import org.testng.annotations.Test;

import java.io.FileInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class GoloClassLoaderTest {

  private static final String SRC = "src/test/resources/for-execution/";

  @Test
  public void check_load() throws Throwable {
    GoloClassLoader classLoader = new GoloClassLoader();
    Class<?> clazz = classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
    assertThat(clazz, notNullValue());
    assertThat(clazz.getName(), is("golotest.execution.FunctionsWithReturns"));
  }

  @Test(expectedExceptions = LinkageError.class)
  public void loading_twice_shall_fail() throws Throwable {
    GoloClassLoader classLoader = new GoloClassLoader();
    classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
    classLoader.load("returns.golo", new FileInputStream(SRC + "returns.golo"));
  }
}
