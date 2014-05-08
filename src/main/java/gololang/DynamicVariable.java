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

/**
 * A dynamic variable has the semantics of an inheritable thread-local reference.
 * This class is modeled after the eponymous class from the Scala standard library.
 *
 * @see java.lang.InheritableThreadLocal
 */
public final class DynamicVariable {

  private final InheritableThreadLocal<Object> threadLocal;

  /**
   * Creates a new dynamic variable with an initial value.
   *
   * @param init the initial value.
   */
  public DynamicVariable(final Object init) {
    super();
    threadLocal = new InheritableThreadLocal<Object>() {
      @Override
      protected Object initialValue() {
        return init;
      }
    };
  }

  /**
   * Returns the thread-local value of the dynamic variable.
   *
   * @return the value.
   */
  public Object value() {
    return threadLocal.get();
  }

  /**
   * Changes the dynamic variable value. The new value is only visible from the calling thread, and will be seen by
   * future child threads.
   *
   * @param value the new thread-local value.
   * @return this dynamic variable.
   */
  public DynamicVariable value(Object value) {
    threadLocal.set(value);
    return this;
  }

  /**
   * Given a value, calls a function {@code func}. The previous value is put pack as the dynamic variable value
   * once {@code func} has completed.
   *
   * @param value the value for the course of the execution of {@code func}.
   * @param func a 0-arity function.
   * @return the result of the call to {@code func}.
   * @throws Throwable in case an exception occurs.
   */
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
