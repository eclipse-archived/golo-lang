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

package fr.insalyon.citi.golo.runtime;

import static java.util.Arrays.copyOfRange;

public class ArrayHelper {

  public static Object head(Object[] array) {
    if (array.length == 0) {
      return null;
    }
    return array[0];
  }

  public static Object[] tail(Object[] array) {
    if (array.length >= 1) {
      return copyOfRange(array, 1, array.length);
    }
    return new Object[0];
  }

  public static boolean isEmpty(Object[] array) {
    return array.length == 0;
  }
}
