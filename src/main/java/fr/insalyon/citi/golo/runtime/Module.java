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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Module {

  static String[] imports(Class<?> callerClass) {
    String[] imports;
    try {
      Method $imports = callerClass.getMethod("$imports");
      imports = (String[]) $imports.invoke(null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $imports() static method.
      imports = new String[]{};
    }
    return imports;
  }

  static String[] augmentations(Class<?> callerClass) {
    String[] augmentations;
    try {
      Method $augmentations = callerClass.getMethod("$augmentations");
      augmentations = (String[]) $augmentations.invoke(null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      augmentations = new String[]{};
    }
    return augmentations;
  }
}
