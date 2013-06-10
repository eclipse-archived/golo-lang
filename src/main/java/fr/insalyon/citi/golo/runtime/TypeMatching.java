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

package fr.insalyon.citi.golo.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class TypeMatching {

  private static final Map<Class, Class> PRIMITIVE_MAP = new HashMap<Class, Class>() {
    {
      put(byte.class, Byte.class);
      put(short.class, Short.class);
      put(char.class, Character.class);
      put(int.class, Integer.class);
      put(long.class, Long.class);
      put(float.class, Float.class);
      put(double.class, Double.class);
      put(boolean.class, Boolean.class);
    }
  };

  static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Constructor constructor, Class<?>[] parameterTypes) {
    return constructor.isVarArgs() && (arguments.length >= parameterTypes.length);
  }

  static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Method method, Class<?>[] parameterTypes) {
    return method.isVarArgs() && (arguments.length >= (parameterTypes.length - 1));
  }

  static boolean haveSameNumberOfArguments(Object[] arguments, Class<?>[] parameterTypes) {
    return parameterTypes.length == arguments.length;
  }

  static boolean canAssign(Class<?>[] types, Object[] arguments, boolean varArgs) {
    if (types.length == 0 || arguments.length == 0) {
      return true;
    }
    for (int i = 0; i < types.length - 1; i++) {
      if (!valueAndTypeMatch(types[i], arguments[i])) {
        return false;
      }
    }
    final int last = types.length - 1;
    if (last >= arguments.length) {
      return false;
    }
    if (varArgs) {
      return valueAndTypeMatch(types[last].getComponentType(), arguments[last]);
    }
    return valueAndTypeMatch(types[last], arguments[last]);
  }

  private static boolean valueAndTypeMatch(Class<?> type, Object value) {
    return primitiveCompatible(type, value) || (type.isInstance(value) || value == null);
  }

  private static boolean primitiveCompatible(Class<?> type, Object value) {
    if (!type.isPrimitive() || value == null) {
      return false;
    }
    return PRIMITIVE_MAP.get(type) == value.getClass();
  }
}
