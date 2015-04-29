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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionReferenceTest {

  public static class Foo {

    public static Object ping(Object obj) {
      return obj;
    }
  }

  private static final MethodHandle ping;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      ping = lookup.findStatic(Foo.class, "ping", MethodType.genericMethodType(1));
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
}