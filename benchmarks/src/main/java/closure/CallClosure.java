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

package closure;

public class CallClosure {

  public static Object run_boxed() {
    Object i = Integer.valueOf(0);
    Object result = null;
    while ((Integer) i < Integer.valueOf(2000000)) {
      result = closure(i);
      i = (Integer) i + 1;
    }
    return ">>> " + result;
  }

  public static Object run_unboxed() {
    Object result = null;
    for (int i = 0; i < 2000000; i++) {
      result = closure(i);
    }
    return result;
  }

  private static Object closure(Object value) {
    return "[" + value + "]";
  }
}
