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

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.util.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

class ArrayMethodFinder implements MethodFinder {

  private Class<?> receiverClass;
  private Object[] args;
  private MethodType type;
  private int arity;
  private String name;
  private Lookup lookup;

  private void init(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    this.args = args;
    this.receiverClass = receiverClass;
    this.type = inlineCache.type();
    this.arity = args.length - 1;
    this.name = inlineCache.name;
    this.lookup = inlineCache.callerLookup;
  }

  private void clean() {
    this.args = null;
    this.receiverClass = null;
    this.type = null;
    this.name = null;
    this.lookup = null;
  }
  
  private void checkArity(int value) {
    if (arity != value) {
      throw new UnsupportedOperationException(name + " on arrays takes " 
          + (value == 0 ? "no" : value)
          + " parameter" + (value > 1 ? "s" : "")
      );
    }
  }

  public MethodHandle find(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    init(inlineCache, receiverClass, args);
    try {
      return resolve().asType(type);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error(e);
    } finally {
      clean();
    }
  }

  private MethodHandle resolve() throws NoSuchMethodException, IllegalAccessException {
    switch (name) {
      case "get":
        checkArity(1);
        return MethodHandles.arrayElementGetter(receiverClass);
      case "set":
        checkArity(2);
        return MethodHandles.arrayElementSetter(receiverClass);
      case "size":
      case "length":
        checkArity(0);
        return lookup.findStatic(Array.class, "getLength", methodType(int.class, Object.class));
      case "iterator":
        checkArity(0);
        return lookup.findConstructor(PrimitiveArrayIterator.class, methodType(void.class, Object[].class));
      case "toString":
        checkArity(0);
        return lookup.findStatic(Arrays.class, "toString", methodType(String.class, Object[].class));
      case "asList":
        checkArity(0);
        return lookup.findStatic(
            Arrays.class, "asList", methodType(List.class, Object[].class))
            .asFixedArity();
      case "equals":
        checkArity(1);
        return lookup.findStatic(Arrays.class, "equals", methodType(boolean.class, Object[].class, Object[].class));
      case "getClass":
        checkArity(0);
        return MethodHandles.dropArguments(MethodHandles.constant(Class.class, receiverClass), 0, receiverClass);
      default:
        throw new UnsupportedOperationException(name + " is not supported on arrays");
    }
  }  
}

