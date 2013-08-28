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

package fibonacci;

import java.lang.reflect.Method;

public class Fibonacci {

  private static final Method fibMethod;

  static {
    try {
      fibMethod = Fibonacci.class.getMethod("fib_reflective", int.class);
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
  }

  public static int fib_unboxed(int n) {
    if (n < 2) {
      return n;
    } else {
      return fib_unboxed(n - 1) + fib_unboxed(n - 2);
    }
  }

  public static Object fib_boxed(Object arg) {
    Integer n = (Integer) arg;
    if (n < 2) {
      return n;
    } else {
      return ((Integer) fib_boxed(n - 1)) + ((Integer) fib_boxed(n - 2));
    }
  }

  public static int fib_reflective(int n) throws Throwable {
    if (n < 2) {
      return n;
    } else {
      return ((int) fibMethod.invoke(null, n - 1)) + ((int) fibMethod.invoke(null, n - 2));
    }
  }
}
