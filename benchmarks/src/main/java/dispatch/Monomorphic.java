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

package dispatch;

import java.lang.reflect.Method;

public class Monomorphic {

  public static Object run_boxed() {
    Object i = Integer.valueOf(0);
    Object result = null;
    while ((Integer) i < Integer.valueOf(5000000)) {
      result = i.toString();
      i = (Integer) i + 1;
    }
    return result;
  }

  public static Object run_reflective() throws Throwable {
    Method toStringMethod = Integer.class.getMethod("toString");
    int i = 0;
    String result = "";
    while (i < 5000000) {
      result = (String) toStringMethod.invoke(Integer.valueOf(i));
      i = i + 1;
    }
    return result;
  }

  public static String run_unboxed() {
    int i = 0;
    String result = "";
    while (i < 5000000) {
      result = Integer.valueOf(i).toString(); // Note: String.valueOf(int) would do an invokestatic call site
      i = i + 1;
    }
    return result;
  }
}
