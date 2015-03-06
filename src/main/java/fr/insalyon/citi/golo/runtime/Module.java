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

package fr.insalyon.citi.golo.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Module {

  private static final Class<?>[] EMPTY_TYPES = new Class<?>[]{};
  private static final Object[] EMPTY_ARGS = new Object[]{};

  static String[] metadata(String name, Class<?> callerClass, Class<?>[] types, Object[] args) {
    String[] data;
    try {
      Method $data = callerClass.getMethod("$" + name, types);
      data = (String[]) $data.invoke(null, args);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $<name>() static method.
      data = new String[]{};
    }
    return data;
  }

  static String[] imports(Class<?> callerClass) {
    return metadata("imports", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  static String[] augmentations(Class<?> callerClass) {
    return metadata("augmentations", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  static String[] augmentationApplications(Class<?> callerClass) {
    return metadata("augmentationApplications", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  static String[] augmentationApplications(Class<?> callerClass, Class<?> receiverClass) {
    return metadata("augmentationApplications", callerClass,
        new Class<?>[] {int.class},
        new Object[]{receiverClass.getName().hashCode()}
    );
  }
}
