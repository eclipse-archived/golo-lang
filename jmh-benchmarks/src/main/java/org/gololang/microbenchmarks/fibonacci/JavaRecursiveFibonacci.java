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

package org.gololang.microbenchmarks.fibonacci;

public class JavaRecursiveFibonacci {

  public static long withPrimitives(long n) {
    if (n <= 2L) {
      return 1L;
    } else {
      return withPrimitives(n - 1L) + withPrimitives(n - 2L);
    }
  }

  // Note: the explicit boxing is voluntary
  public static Long withBoxing(Long n) {
    if (n.longValue() <= Long.valueOf(2L)) {
      return Long.valueOf(1L);
    } else {
      return Long.valueOf(withBoxing(Long.valueOf(n.longValue() - Long.valueOf(1L))) + withBoxing(Long.valueOf(n.longValue() - Long.valueOf(2L))));
    }
  }
}
