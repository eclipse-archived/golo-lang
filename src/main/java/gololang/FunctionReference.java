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

package gololang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.insertArguments;

/**
 * A reference to a function / closure.
 *
 * This class essentially boxes {@code MethodHandle} references, and provides as many delegations as possible.
 * Previous versions of Golo used direct {@code MethodHandle} objects to deal with functions by reference, but that
 * class does not provide any mean to attach local state, as required for, say, implementing named arguments.
 *
 * This boxed representation provides a sound abstraction while not hurting performance, as
 * {@code fr.insalyon.citi.golo.runtime.ClosureCallSupport} still dispatches through a method handle.
 *
 * @see java.lang.invoke.MethodHandle
 * @see fr.insalyon.citi.golo.runtime.ClosureCallSupport
 */
public class FunctionReference {

  private final MethodHandle handle;

  /**
   * Makes a function reference from a method handle.
   *
   * @param handle the method handle.
   * @throws IllegalArgumentException if {@code handle} is {@code null}.
   */
  public FunctionReference(MethodHandle handle) {
    if (handle == null) {
      throw new IllegalArgumentException("A method handle cannot be null");
    }
    this.handle = handle;
  }

  /**
   * Unboxes the method handle.
   *
   * @return the (boxed) method handle.
   */
  public MethodHandle handle() {
    return handle;
  }

  public MethodType type() {
    return handle.type();
  }

  public FunctionReference asCollector(Class<?> arrayType, int arrayLength) {
    return new FunctionReference(handle.asCollector(arrayType, arrayLength));
  }

  public FunctionReference asFixedArity() {
    return new FunctionReference(handle.asFixedArity());
  }

  public FunctionReference asType(MethodType newType) {
    return new FunctionReference(handle.asType(newType));
  }

  public FunctionReference asVarargsCollector(Class<?> arrayType) {
    return new FunctionReference(handle.asVarargsCollector(arrayType));
  }

  public FunctionReference bindTo(Object x) {
    return new FunctionReference(handle.bindTo(x));
  }

  public boolean isVarargsCollector() {
    return handle.isVarargsCollector();
  }

  public FunctionReference asSpreader(Class<?> arrayType, int arrayLength) {
    return new FunctionReference(handle.asSpreader(arrayType, arrayLength));
  }

  public Object invoke(Object... args) throws Throwable {
    return handle.invokeWithArguments(args);
  }

  @Override
  public String toString() {
    return "FunctionReference{" +
        "handle=" + handle +
        '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FunctionReference that = (FunctionReference) obj;
    return handle.equals(that.handle);
  }

  @Override
  public int hashCode() {
    return handle.hashCode();
  }

  /**
   * Converts a function reference to an instance of an interface.
   *
   * @param interfaceClass the interface,
   * @return a proxy object that satisfies {@code interfaceClass} and delegates to {@code this}.
   */
  public Object to(Class<?> interfaceClass) {
    return Predefined.asInterfaceInstance(interfaceClass, this);
  }

  /**
   * Compose a function with another function.
   *
   * @param fun the function that processes the results of {@code this} function.
   * @return a composed function.
   */
  public FunctionReference andThen(FunctionReference fun) {
    return new FunctionReference(filterReturnValue(this.handle, fun.handle));
  }

  /**
   * Partial application.
   *
   * @param position the argument position (0-indexed).
   * @param value the argument value.
   * @return a partially applied function.
   */
  public FunctionReference bindAt(int position, Object value) {
    return new FunctionReference(MethodHandles.insertArguments(this.handle, position, value));
  }

  /**
   * Partial application.
   *
   * @param position the first argument position.
   * @param values the values of the arguments from {@code position}.
   * @return a partially applied function.
   * @see java.lang.invoke.MethodHandles#insertArguments(MethodHandle, int, Object...)
   */
  public FunctionReference insertArguments(int position, Object... values) {
    return new FunctionReference(MethodHandles.insertArguments(handle, position, values));
  }

  /**
   * Spread arguments over this function parameters.
   *
   * @param arguments arguments as an array.
   * @return a return value.
   * @throws Throwable ...because an exception can be thrown.
   */
  public Object spread(Object... arguments) throws Throwable {
    int arity = this.handle.type().parameterCount();
    if (this.handle.isVarargsCollector() && (arity > 0) && (arguments[arity - 1] instanceof Object[])) {
      return this.handle
          .asFixedArity()
          .asSpreader(Object[].class, arguments.length)
          .invoke(arguments);
    }
    return this.handle
        .asSpreader(Object[].class, arguments.length)
        .invoke(arguments);
  }
}
