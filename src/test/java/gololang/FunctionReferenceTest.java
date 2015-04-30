/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodType.genericMethodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
 * Note: most tests are to be run from Golo code and CompileAndRunTest.
 */
public class FunctionReferenceTest {

  public static class Foo {

    public static Object ping(Object obj) {
      return obj;
    }

    public static Object collect(Object a, Object b, Object c) {
      return String.valueOf(a) + b + c;
    }

    public static Object collectN(Object a, Object... b) {
      String head = (String) a;
      String[] tail = new String[b.length];
      for (int i = 0; i < b.length; i++) {
        tail[i] = (String) b[i];
      }
      return head + Arrays.stream(tail).collect(Collectors.joining());
    }
  }

  private static final MethodHandle ping;
  private static final MethodHandle collect;
  private static final MethodHandle collectN;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      ping = lookup.findStatic(Foo.class, "ping", genericMethodType(1));
      collect = lookup.findStatic(Foo.class, "collect", genericMethodType(3));
      collectN = lookup.findStatic(Foo.class, "collectN", genericMethodType(1, true));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void sanity_check() throws Throwable {
    FunctionReference fun = new FunctionReference(ping);
    assertThat(fun.handle().invoke("Plop"), is("Plop"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void refuse_null_handles() {
    new FunctionReference(null);
  }

  @Test
  public void spread() throws Throwable {
    FunctionReference fun = new FunctionReference(collect);
    assertThat(fun.handle().invoke(1, 2, 3), is("123"));
    assertThat(fun.spread(1, 2, 3), is("123"));
  }

  @Test
  public void spread_varargs() throws Throwable {
    FunctionReference fun = new FunctionReference(collectN);
    assertThat(fun.handle().invoke("1", "2", "3"), is("123"));
    assertThat(fun.spread("1", new Object[]{"2", "3"}), is("123"));
  }
}