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
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.insertArguments;

public final class FunctionReference {

  private final MethodHandle handle;

  public FunctionReference(MethodHandle handle) {
    if (handle == null) {
      throw new IllegalArgumentException("A method handle cannot be null");
    }
    this.handle = handle;
  }

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

  public Object to(Class<?> interfaceClass) {
    return Predefined.asInterfaceInstance(interfaceClass, this);
  }

  public FunctionReference andThen(FunctionReference fun) {
    return new FunctionReference(filterReturnValue(this.handle, fun.handle));
  }

  public FunctionReference bindAt(int position, Object value) {
    return new FunctionReference(insertArguments(this.handle, position, value));
  }

  public FunctionReference spread(Object... arguments) throws Throwable {
    int arity = this.handle.type().parameterCount();
    if (this.handle.isVarargsCollector() && (arity > 0) && (arguments[arity - 1] instanceof Object[])) {
      MethodHandle spread = (MethodHandle) this.handle
          .asFixedArity()
          .asSpreader(Object[].class, arguments.length)
          .invokeWithArguments(arguments);
      return new FunctionReference(spread);
    }
    MethodHandle spread = (MethodHandle) this.handle
        .asSpreader(Object[].class, arguments.length)
        .invokeWithArguments(arguments);
    return new FunctionReference(spread);
  }
}
