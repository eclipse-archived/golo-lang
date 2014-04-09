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

package gololang;

import java.lang.invoke.MethodHandle;

public final class DynamicVariable {

  private final InheritableThreadLocal<Object> threadLocal;

  public DynamicVariable(final Object init) {
    super();
    threadLocal = new InheritableThreadLocal<Object>() {
      @Override
      protected Object initialValue() {
        return init;
      }
    };
  }

  public Object value() {
    return threadLocal.get();
  }

  public DynamicVariable value(Object value) {
    threadLocal.set(value);
    return this;
  }

  public Object withValue(Object value, MethodHandle func) throws Throwable {
    if (func.type().parameterCount() != 0) {
      throw new IllegalArgumentException("withValue requires a function with no parameters");
    }
    Object oldValue = value();
    this.value(value);
    try {
      return func.invoke();
    } finally {
      this.value(oldValue);
    }
  }

  @Override
  public String toString() {
    return "DynamicVariable{" +
        "value=" + value() +
        '}';
  }
}
