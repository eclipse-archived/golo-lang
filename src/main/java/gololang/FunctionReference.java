/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.filterReturnValue;

/**
 * A reference to a function / closure.
 *
 * This class essentially boxes {@code MethodHandle} references, and provides as many delegations as possible.
 * Previous versions of Golo used direct {@code MethodHandle} objects to deal with functions by reference, but that
 * class does not provide any mean to attach local state, as required for, say, implementing named arguments.
 *
 * This boxed representation provides a sound abstraction while not hurting performance, as
 * {@code org.eclipse.golo.runtime.ClosureCallSupport} still dispatches through a method handle.
 *
 * @see java.lang.invoke.MethodHandle
 * @see org.eclipse.golo.runtime.ClosureCallSupport
 */
public class FunctionReference {

  private final MethodHandle handle;

  private final String[] parameterNames;

  /**
   * Makes a function reference from a method handle.
   *
   * @param handle the method handle.
   * @param parameterNames the target method parameter's names.
   * @throws IllegalArgumentException if {@code handle} is {@code null}.
   */
  public FunctionReference(MethodHandle handle, String[] parameterNames) {
    if (handle == null) {
      throw new IllegalArgumentException("A method handle cannot be null");
    }
    this.handle = handle;
    this.parameterNames = parameterNames;
  }

  /**
   * Makes a function reference from a method handle.
   * The parameter names will be {@code null}.
   *
   * @param handle the method handle.
   * @throws IllegalArgumentException if {@code handle} is {@code null}.
   */
  public FunctionReference(MethodHandle handle) {
    this(handle, null);
  }

  /**
   * Unboxes the method handle.
   *
   * @return the (boxed) method handle.
   */
  public MethodHandle handle() {
    return handle;
  }

  /**
   * Get the target function parameter's names
   *
   * @return the array of parameter's names
   */
  public String[] parameterNames() {
    return Arrays.copyOf(parameterNames, parameterNames.length);
  }

  public MethodType type() {
    return handle.type();
  }

  public FunctionReference asCollector(Class<?> arrayType, int arrayLength) {
    return new FunctionReference(handle.asCollector(arrayType, arrayLength), this.parameterNames);
  }

  public FunctionReference asCollector(int arrayLength) {
    return asCollector(Object[].class, arrayLength);
  }

  public FunctionReference asFixedArity() {
    return new FunctionReference(handle.asFixedArity(), this.parameterNames);
  }

  public FunctionReference asType(MethodType newType) {
    return new FunctionReference(handle.asType(newType), this.parameterNames);
  }

  public FunctionReference asVarargsCollector(Class<?> arrayType) {
    return new FunctionReference(handle.asVarargsCollector(arrayType), this.parameterNames);
  }

  public FunctionReference asVarargsCollector() {
    return asVarargsCollector(Object[].class);
  }

  public FunctionReference bindTo(Object x) {
    return new FunctionReference(handle.bindTo(x), dropParameterNames(0, 1));
  }

  public boolean isVarargsCollector() {
    return handle.isVarargsCollector();
  }

  public FunctionReference asSpreader(Class<?> arrayType, int arrayLength) {
    return new FunctionReference(handle.asSpreader(arrayType, arrayLength));
  }

  public FunctionReference asSpreader(int arrayLength) {
    return asSpreader(Object[].class, arrayLength);
  }

  public FunctionReference asSpreader() {
    return asSpreader(Object[].class, arity());
  }

  /**
   * Returns the arity of the function.
   *
   * The arity is the number of declared parameter in the function signature.
   *
   * @return the number of declared parameter
   */
  public int arity() {
    return handle.type().parameterCount();
  }

  /**
   * Check if this function can be invoked with the given number of arguments.
   */
  public boolean acceptArity(int nb) {
    return arity() == nb || (arity() == nb + 1 && isVarargsCollector());
  }

  public Object invoke(Object... args) throws Throwable {
    return handle.invokeWithArguments(args);
  }

  /**
   * Apply the function to the provided arguments.
   *
   * If the number of arguments corresponds to the function arity, the function is applied.
   * Otherwise, a function partialized with the given arguments is returned.
   * @return the result of the function or a partialized version of the function
   */
  public Object invokeOrBind(Object... args) throws Throwable {
    if (args.length < arity()) {
      return insertArguments(0, args);
    }
    return handle.invokeWithArguments(args);
  }

  @Override
  public String toString() {
    return "FunctionReference{" +
        "handle=" + (handle.isVarargsCollector() ? "(varargs)" : "") + handle +
        ", parameterNames=" + Arrays.toString(parameterNames) +
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
    MethodHandle other = null;
    if (fun.isVarargsCollector() && fun.arity() == 1) {
      other = fun.handle.asCollector(Object[].class, 1);
    } else if (fun.isVarargsCollector() && fun.arity() == 2) {
      other = MethodHandles.insertArguments(fun.handle, 1, new Object[]{new Object[0]});
    } else if (fun.arity() == 1) {
      other = fun.handle;
    } else {
      throw new IllegalArgumentException("`andThen` requires a function that can be applied to 1 parameter");
    }
    return new FunctionReference(filterReturnValue(this.handle, other), this.parameterNames);
  }

  /*
   * Compose a function with another function.
   *
   * <p>This is equivalent to {@code fun.andThen(this)}.
   *
   * @param fun the function to apply before {@code this} function.
   * @return a composed function.
   */
  public FunctionReference compose(FunctionReference fun) {
    if (!acceptArity(1)) {
      throw new UnsupportedOperationException("`compose` must be called on function accepting 1 parameter");
    }
    return fun.andThen(this);
  }

  /**
   * Partial application.
   *
   * @param position the argument position (0-indexed).
   * @param value the argument value.
   * @return a partially applied function.
   */
  public FunctionReference bindAt(int position, Object value) {
    return new FunctionReference(MethodHandles.insertArguments(this.handle, position, value), dropParameterNames(position, 1));
  }

  /**
   * Partial application based on parameter's names.
   *
   * @param parameterName the parameter to bind.
   * @param value the argument value.
   * @return a partially applied function.
   */
  public FunctionReference bindAt(String parameterName, Object value) {
    int position = -1;
    if (this.parameterNames == null) {
      throw new RuntimeException("Can't bind on parameter name, " + this.toString() + " has none");
    }
    for (int i = 0; i < this.parameterNames.length; i++) {
      if (this.parameterNames[i].equals(parameterName)) {
        position = i;
        break;
      }
    }
    if (position == -1) {
      throw new IllegalArgumentException("'" + parameterName + "' not in the parameter list " + Arrays.toString(parameterNames));
    }
    return bindAt(position, value);
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
    if (values.length == 0) {
      return this;
    }
    MethodHandle bounded = MethodHandles.insertArguments(handle, position, values);
    if (handle.isVarargsCollector()) {
      bounded = bounded.asVarargsCollector(Object[].class);
    }
    return new FunctionReference(bounded, dropParameterNames(position, values.length));
  }

  /**
   * Spread arguments over this function parameters.
   *
   * @param arguments arguments as an array.
   * @return a return value.
   * @throws Throwable ...because an exception can be thrown.
   */
  public Object spread(Object... arguments) throws Throwable {
    int arity = arity();
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

  private String[] dropParameterNames(int from, int size) {
    if (this.parameterNames == null) {
      return null;
    }
    String[] filtered = new String[this.parameterNames.length - size];
    if (filtered.length > 0) {
      System.arraycopy(parameterNames, 0, filtered, 0, from);
      System.arraycopy(parameterNames, from + size, filtered, from, this.parameterNames.length - size - from);
    }
    return filtered;
  }
}
