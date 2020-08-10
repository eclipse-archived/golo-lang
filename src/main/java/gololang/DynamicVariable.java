/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

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
  public Object withValue(Object value, FunctionReference func) throws Throwable {
    if (!func.acceptArity(0)) {
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
    return String.format("DynamicVariable{value=%s}", value());
  }
}
