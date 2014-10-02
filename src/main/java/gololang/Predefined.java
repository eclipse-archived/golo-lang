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

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <code>Predefined</code> provides the module of predefined functions in Golo. The provided module is imported by
 * default.
 */
public class Predefined {

  private Predefined() {
    throw new UnsupportedOperationException("Why on earth are you trying to instantiate this class?");
  }

  // ...................................................................................................................

  /**
   * Raises a <code>RuntimeException</code> with a message.
   *
   * @param message the exception description.
   * @throws RuntimeException (always)
   */
  public static void raise(Object message) {
    require(message instanceof String, "raise requires a message as a String");
    throw new RuntimeException((String) message);
  }

  /**
   * Raises a <code>RuntimeException</code> with a message and a cause.
   *
   * @param message the exception description.
   * @param cause   the exception cause.
   * @throws RuntimeException (always)
   */
  public static void raise(Object message, Object cause) {
    require(message instanceof String, "raise requires a message as a String");
    require(cause instanceof Throwable, "raise requires a cause as a Throwable");
    throw new RuntimeException((String) message, (Throwable) cause);
  }

  // ...................................................................................................................

  /**
   * Prints to the standard console.
   *
   * @param obj the object to be printed.
   */
  public static void print(Object obj) {
    System.out.print(obj);
  }

  /**
   * Prints to the standard console, including a newline.
   *
   * @param obj obj the object to be printed.
   */
  public static void println(Object obj) {
    System.out.println(obj);
  }

  /**
   * Reads the next line of characters from the console.
   *
   * @return a String.
   */
  public static String readln() throws IOException {
    return System.console().readLine();
  }

  /**
   * Reads the next line of characters from the console.
   *
   * @param message displays a prompt message.
   * @return a String.
   */
  public static String readln(String message) throws IOException {
    System.out.print(message);
    return readln();
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @return a String.
   */
  public static String readPassword() throws IOException {
    return String.valueOf(System.console().readPassword());
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @param message displays a prompt message.
   * @return a String.
   */
  public static String readPassword(String message) throws IOException {
    System.out.print(message);
    return readPassword();
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @return a character array.
   */
  public static char[] secureReadPassword() throws IOException {
    return System.console().readPassword();
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @param message displays a prompt message.
   * @return a character array.
   */
  public static char[] secureReadPassword(String message) throws IOException {
    System.out.print(message);
    return secureReadPassword();
  }


  // ...................................................................................................................

  /**
   * Requires that an object is not the <code>null</code> reference.
   *
   * @param obj the object to test against <code>null</code>.
   * @throws AssertionError if <code>obj</code> is <code>null</code>.
   */
  public static void requireNotNull(Object obj) throws AssertionError {
    if (obj != null) {
      return;
    }
    throw new AssertionError("null reference encountered");
  }

  /**
   * Requires that a condition be <code>true</code>.
   *
   * @param condition    the condition, must be a <code>Boolean</code>.
   * @param errorMessage the error message, must be a <code>String</code>.
   * @throws IllegalArgumentException if the arguments are of the wrong type.
   * @throws AssertionError           if <code>condition</code> is <code>false</code>.
   */
  public static void require(Object condition, Object errorMessage) throws IllegalArgumentException, AssertionError {
    requireNotNull(condition);
    requireNotNull(errorMessage);
    if ((condition instanceof Boolean) && (errorMessage instanceof String)) {
      if ((Boolean) condition) {
        return;
      }
      throw new AssertionError(errorMessage);
    } else {
      throw new IllegalArgumentException(
          new StringBuilder()
              .append("Wrong parameters for require: expected (Boolean, String) but got (")
              .append(condition.getClass().getName())
              .append(", ")
              .append(errorMessage.getClass().getName())
              .append(")")
              .toString());
    }
  }

  // ...................................................................................................................

  /**
   * Makes a Java primitive array out of values.
   *
   * @param values the values.
   * @return an array.
   */
  public static Object Array(Object... values) {
    return values;
  }

  /**
   * Makes a list out of a Java primitive array.
   *
   * @param values the array.
   * @return a list from the <code>java.util</code> package.
   */
  public static Object atoList(Object[] values) {
    return Arrays.asList(values);
  }

  /**
   * Access an array element by index.
   *
   * @param a the array.
   * @param i the index.
   * @return the element at index <code>i</code>.
   */
  public static Object aget(Object a, Object i) {
    require(a instanceof Object[], "aget takes an Array as first parameter");
    require(i instanceof Integer, "aget takes an index as second parameter");
    Object[] array = (Object[]) a;
    return array[(Integer) i];
  }

  /**
   * Updates an array element by index.
   *
   * @param a     the array.
   * @param i     the index.
   * @param value the new value.
   */
  public static void aset(Object a, Object i, Object value) {
    require(a instanceof Object[], "aset takes an Array as first parameter");
    require(i instanceof Integer, "aset takes an index as second parameter");
    Object[] array = (Object[]) a;
    array[(Integer) i] = value;
  }

  /**
   * Array length.
   *
   * @param a the array.
   * @return the length of <code>a</code>.
   */
  public static Object alength(Object a) {
    require(a instanceof Object[], "alength takes an Array as parameter");
    Object[] array = (Object[]) a;
    return array.length;
  }

  // ...................................................................................................................

  /**
   * Makes an integer range object between two bounds. Range objects implement <code>java.lang.Iterable</code>, so
   * they can be used in Golo <code>foreach</code> loops.
   *
   * @param from the lower-bound (inclusive) as an <code>Integer</code> or <code>Long</code>.
   * @param to   the upper-bound (exclusive) as an <code>Integer</code> or <code>Long</code>.
   * @return a range object.
   * @see java.lang.Iterable
   */
  public static Object range(Object from, Object to) {
    require((from instanceof Integer) || (from instanceof Long), "from must either be an Integer or a Long");
    require((to instanceof Integer) || (to instanceof Long), "to must either be an Integer or a Long");
    if ((to instanceof Long) && (from instanceof Long)) {
      return new LongRange((Long) from, (Long) to);
    } else if ((to instanceof Integer) && (from instanceof Integer)) {
      return new IntRange((Integer) from, (Integer) to);
    } else if (from instanceof Long) {
      return new LongRange((Long) from, (Integer) to);
    } else {
      return new LongRange((Integer) from, (Long) to);
    }
  }

  // ...................................................................................................................

  /**
   * Makes a key / value pair.
   *
   * @param key   the key.
   * @param value the value.
   * @return an instance of <code>AbstractMap.SimpleEntry</code>
   * @see java.util.AbstractMap.SimpleEntry
   */
  public static Object mapEntry(Object key, Object value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  // ...................................................................................................................

  /**
   * Turns a method handle into a instance of a single-method interface.
   *
   * @param interfaceClass the target single-method interface class.
   * @param target         the implementation method handle.
   * @return an instance of <code>interfaceClass</code>.
   * @see java.lang.invoke.MethodHandleProxies#asInterfaceInstance(Class, java.lang.invoke.MethodHandle)
   */
  public static Object asInterfaceInstance(Object interfaceClass, Object target) {
    require(interfaceClass instanceof Class, "interfaceClass must be a Class");
    require(target instanceof MethodHandle, "target must be a MethodHandle");
    return MethodHandleProxies.asInterfaceInstance((Class<?>) interfaceClass, (MethodHandle) target);
  }

  /**
   * Test whether an object is a closure or not.
   *
   * @param object the object.
   * @return <code>true</code> if <code>object</code> is an instance of <code>java.lang.invoke.MethodHandle</code>,
   * <code>false</code> otherwise.
   */
  public static Object isClosure(Object object) {
    return object instanceof MethodHandle;
  }

  // ...................................................................................................................

  /**
   * Obtains a method handle / closure to a function.
   *
   * @param name   the function name.
   * @param module the function enclosing module (a Java class).
   * @param arity  the function arity, where a negative value means that any arity will do.
   * @return a method handle to the matched function.
   * @throws NoSuchMethodException    if the target function could not be found.
   * @throws IllegalArgumentException if the argument types are not of types <code>(String, Class, Integer)</code>.
   * @throws Throwable                if an error occurs.
   */
  public static Object fun(Object name, Object module, Object arity) throws Throwable {
    require(name instanceof String, "name must be a String");
    require(module instanceof Class, "module must be a module (e.g., foo.bar.Some.module)");
    require(arity instanceof Integer, "name must be an Integer");
    Class<?> moduleClass = (Class<?>) module;
    String functionName = (String) name;
    int functionArity = (Integer) arity;
    Method targetMethod = null;
    List<Method> candidates = new LinkedList<>(Arrays.asList(moduleClass.getDeclaredMethods()));
    candidates.addAll(Arrays.asList(moduleClass.getMethods()));
    for (Method method : candidates) {
      if (method.getName().equals(functionName) && Modifier.isStatic(method.getModifiers())) {
        if ((functionArity < 0) || (method.getParameterTypes().length == functionArity)) {
          targetMethod = method;
          break;
        }
      }
    }
    if (targetMethod != null) {
      MethodHandles.Lookup lookup = MethodHandles.publicLookup();
      targetMethod.setAccessible(true);
      return lookup.unreflect(targetMethod);
    }
    throw new NoSuchMethodException((name + " in " + module + ((functionArity < 0) ? "" : (" with arity " + functionArity))));
  }

  /**
   * Obtains the first method handle / closure to a function.
   * <p>
   * This is the same as calling <code>fun(name, module, -1)</code>.
   *
   * @see Predefined#fun(Object, Object, Object)
   */
  public static Object fun(Object name, Object module) throws Throwable {
    return fun(name, module, -1);
  }

  // ...................................................................................................................

  /**
   * Reads the content of a text file.
   *
   * @param file     the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @param encoding the file encoding as a {@link String} or {@link Charset}.
   * @return the content as a {@link String}.
   */
  public static Object fileToText(Object file, Object encoding) throws Throwable {
    Path path = pathFrom(file);
    Charset charset = null;
    if (encoding instanceof String) {
      charset = Charset.forName((String) encoding);
    } else if (encoding instanceof Charset) {
      charset = (Charset) encoding;
    } else {
      throw new IllegalArgumentException("encoding must be either a string or a charset instance");
    }
    return new String(Files.readAllBytes(path), charset);
  }

  private static Path pathFrom(Object file) {
    if (file instanceof String) {
      return Paths.get((String) file);
    } else if (file instanceof File) {
      return ((File) file).toPath();
    } else if (file instanceof Path) {
      return (Path) file;
    }
    throw new IllegalArgumentException("file must be a string, a file or a path");
  }

  /**
   * Writes some text to a file. The file is created if it does not exist, and overwritten if it already exists.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   */
  public static void textToFile(Object text, Object file) throws Throwable {
    require(text instanceof String, "text must be a string");
    String str = (String) text;
    Path path = pathFrom(file);
    if (path.toString().equals("-")) {
      System.out.write(str.getBytes());
    } else {
      Files.write(path, str.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  /**
   * Check if a file exists.
   *
   * @param file the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @return true if the file exists, false if it doesn't
   */
  public static boolean fileExists(Object file) {
    return Files.exists(pathFrom(file));
  }

  /**
   * Return current path of execution.
   *
   * @return current path of execution
   */
  public static String currentDir() throws Throwable {
    return new File(".").getCanonicalPath();
  }

  // ...................................................................................................................

  /**
   * Checks if an object is a (JVM) array or not.
   *
   * @param object the object to check.
   * @return {@code true} if {@code object} is an array, {@code false} otherwise or if {@code null}.
   */
  public static boolean isArray(Object object) {
    return (object != null) && object.getClass().isArray();
  }

  /**
   * Function to obtain the {@code Object[].class} reference.
   *
   * @return {@code Object[].class}
   */
  public static Class<?> objectArrayType() {
    return Object[].class;
  }

  /**
   * Returns an array class given a type class.
   *
   * @param klass the array type.
   * @return the class of the array of type {@code klass}, i.e., {@code klass[]}.
   * @throws ClassNotFoundException if the type could not be found.
   */
  public static Class<?> arrayTypeOf(Object klass) throws ClassNotFoundException {
    require(klass instanceof Class<?>, "klass must be a class");
    Class<?> type = (Class<?>) klass;
    return Class.forName("[L" + type.getName() + ";", true, type.getClassLoader());
  }

  // ...................................................................................................................
  // These are generated methods, see src/main/ruby/generate_type_conversions.rb

  /**
   * Gives the Character value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Character value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static Object charValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof Character) {
      return obj;
    }
    if (obj instanceof Integer) {
      int value = (Integer) obj;
      return (char) value;
    }
    if (obj instanceof Long) {
      long value = (Long) obj;
      return (char) value;
    }
    if (obj instanceof Double) {
      double value = (Double) obj;
      return (char) value;
    }
    if (obj instanceof Float) {
      float value = (Float) obj;
      return (char) value;
    }
    if (obj instanceof String) {
      return ((String) obj).charAt(0);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  /**
   * Gives the Integer value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Integer value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static Object intValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof Integer) {
      return obj;
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return (int) value;
    }
    if (obj instanceof Long) {
      long value = (Long) obj;
      return (int) value;
    }
    if (obj instanceof Double) {
      double value = (Double) obj;
      return (int) value;
    }
    if (obj instanceof Float) {
      float value = (Float) obj;
      return (int) value;
    }
    if (obj instanceof String) {
      return Integer.valueOf((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  /**
   * Gives the Long value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Long value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static Object longValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof Long) {
      return obj;
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return (long) value;
    }
    if (obj instanceof Integer) {
      int value = (Integer) obj;
      return (long) value;
    }
    if (obj instanceof Double) {
      double value = (Double) obj;
      return (long) value;
    }
    if (obj instanceof Float) {
      float value = (Float) obj;
      return (long) value;
    }
    if (obj instanceof String) {
      return Long.valueOf((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  /**
   * Gives the Double value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Double value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static Object doubleValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof Double) {
      return obj;
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return (double) value;
    }
    if (obj instanceof Integer) {
      int value = (Integer) obj;
      return (double) value;
    }
    if (obj instanceof Long) {
      long value = (Long) obj;
      return (double) value;
    }
    if (obj instanceof Float) {
      float value = (Float) obj;
      return (double) value;
    }
    if (obj instanceof String) {
      return Double.valueOf((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  /**
   * Gives the Float value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Float value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static Object floatValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof Float) {
      return obj;
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return (float) value;
    }
    if (obj instanceof Integer) {
      int value = (Integer) obj;
      return (float) value;
    }
    if (obj instanceof Long) {
      long value = (Long) obj;
      return (float) value;
    }
    if (obj instanceof Double) {
      double value = (Double) obj;
      return (float) value;
    }
    if (obj instanceof String) {
      return Float.valueOf((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  // ...................................................................................................................
}
