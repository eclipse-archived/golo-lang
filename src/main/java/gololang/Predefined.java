/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.eclipse.golo.runtime.AmbiguousFunctionReferenceException;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.golo.runtime.Extractors;
import org.eclipse.golo.runtime.Loader;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.reflect.Modifier.isStatic;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static java.util.stream.Collectors.toList;

/**
 * <code>Predefined</code> provides the module of predefined functions in Golo. The provided module is imported by
 * default.
 */
public final class Predefined {

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
   * Makes a new typed JVM array.
   *
   * This function simply forwards to {@code java.lang.reflect.Array.newInstance}.
   *
   * @param type   the array type.
   * @param length the array length.
   * @return a new typed array.
   */
  public static Object newTypedArray(Class<?> type, int length) {
    return java.lang.reflect.Array.newInstance(type, length);
  }

  // ...................................................................................................................

  /**
   * Makes an range object between two bounds. Range objects implement
   * <code>java.lang.Collection</code> (immutable), so they can be used in Golo <code>foreach</code>
   * loops.
   *
   * @param from the lower-bound (inclusive) as an <code>Integer</code>, <code>Long</code>, or
   *             <code>Character</code>.
   * @param to   the upper-bound (exclusive) as an <code>Integer</code>, <code>Long</code> or
   *             <code>Character</code>
   * @return a range object.
   * @see java.util.Collection
   */
  public static Object range(Object from, Object to) {
    require((from instanceof Integer) || (from instanceof Long) || (from instanceof Character),
        "from must either be an Integer, Long or Character");
    require((to instanceof Integer) || (to instanceof Long) || (to instanceof Character),
        "to must either be an Integer, Long or Character");

    if ((from instanceof Character && !(to instanceof Character))
        || (to instanceof Character && !(from instanceof Character))) {
      throw new IllegalArgumentException("both bounds must be char for a char range");
    }
    if (to instanceof Character && from instanceof Character) {
      return new CharRange((Character) from, (Character) to);
    }
    if (to instanceof Integer && from instanceof Integer) {
      return new IntRange((Integer) from, (Integer) to);
    }
    if (to instanceof Long && from instanceof Long) {
      return new LongRange((Long) from, (Long) to);
    }
    if (from instanceof Long) {
      return new LongRange((Long) from, (Integer) to);
    }
    return new LongRange((Integer) from, (Long) to);
  }

  /**
   * Makes an range object starting from the default value.
   * <p>
   * The default value is 0 for numbers and 'A' for chars.
   *
   * @param to the upper-bound (exclusive) as an <code>Integer</code> or <code>Long</code>.
   * @return a range object.
   * @see gololang.Predefined#range
   */
  public static Object range(Object to) {
    require((to instanceof Integer) || (to instanceof Long) || (to instanceof Character),
        "to must either be an Integer, Long or Character");
    if (to instanceof Integer) {
      return new IntRange((Integer) to);
    }
    if (to instanceof Long) {
      return new LongRange((Long) to);
    } else {
      return new CharRange((Character) to);
    }
  }

  /**
   * Makes an decreasing range object between two bounds. Range objects implement <code>java.lang.Collection</code>, so
   * they can be used in Golo <code>foreach</code> loops.
   *
   * @param from the upper-bound (inclusive) as an <code>Integer</code>, <code>Long</code> or
   *             <code>Character</code>.
   * @param to   the lower-bound (exclusive) as an <code>Integer</code>, <code>Long</code> or
   *             <code>Character</code>.
   * @return a range object.
   * @see gololang.Predefined#range
   */
  public static Object reversedRange(Object from, Object to) {
    return ((Range<?>) range(from, to)).incrementBy(-1);
  }

  /**
   * Makes an decreasing range object up to the default value.
   * <p>
   * The default value is 0 for numbers and 'A' for chars.
   *
   * @param from the upper-bound (inclusive) as an <code>Integer</code>, <code>Long</code> or
   *             <code>Character</code>.
   * @return a range object.
   * @see gololang.Predefined#reversedRange
   * @see gololang.Predefined#range
   */
  public static Object reversedRange(Object from) {
    return ((Range<?>) range(from)).reversed();
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
   * Turns a function reference into an instance of a single-method interface.
   *
   * @param interfaceClass the target single-method interface class.
   * @param target         the implementation function reference.
   * @return an instance of {@code interfaceClass}.
   * @see java.lang.invoke.MethodHandleProxies#asInterfaceInstance(Class, java.lang.invoke.MethodHandle)
   */
  public static Object asInterfaceInstance(Object interfaceClass, Object target) {
    require(interfaceClass instanceof Class, "interfaceClass must be a Class");
    require(target instanceof FunctionReference, "target must be a FunctionReference");
    return MethodHandleProxies.asInterfaceInstance((Class<?>) interfaceClass, ((FunctionReference) target).handle());
  }

  /**
   * Turns a function reference into an instance of a Java 8 functional interface.
   *
   * The implementation delegates to Golo adapter fabrics.
   *
   * @param type the functional interface class.
   * @param func the implementation function.
   * @return an instance of {@code type}.
   * @throws Throwable if the adaptation fails.
   * @see gololang.GoloAdapter
   */
  public static Object asFunctionalInterface(Object type, Object func) throws Throwable {
    require(type instanceof Class, "type must be a Class");
    require(func instanceof FunctionReference, "func must be a FunctionReference");
    Class<?> theType = (Class<?>) type;
    for (Method method : theType.getMethods()) {
      if (!method.isDefault() && !isStatic(method.getModifiers())) {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("interfaces", new Tuple(theType.getCanonicalName()));
        Map<String, FunctionReference> implementations = new HashMap<>();
        implementations.put(
            method.getName(),
            new FunctionReference(
              dropArguments(((FunctionReference) func).handle(), 0, Object.class),
              Arrays.stream(method.getParameters())
              .map(Parameter::getName)
              .toArray(String[]::new)));
        configuration.put("implements", implementations);
        return new AdapterFabric().maker(configuration).newInstance();
      }
    }
    throw new RuntimeException("Could not convert " + func + " to a functional interface of type " + type);
  }

  /**
   * Test whether an object is a closure or not.
   *
   * @param object the object.
   * @return {@code true} if {@code object} is an instance of {@link gololang.FunctionReference} {@code false} otherwise.
   */
  public static boolean isClosure(Object object) {
    return object instanceof FunctionReference;
  }

  // ...................................................................................................................

  /**
   * Obtains a reference to a function.
   *
   * @param name   the function name.
   * @param module the function enclosing module (a Java class).
   * @param arity  the function arity, where a negative value means that any arity will do.
   * @param varargs if the functions has variable arity.
   * @return a function reference to the matched function.
   * @throws NoSuchMethodException    if the target function could not be found.
   * @throws IllegalArgumentException if the argument types are not of types <code>(String, Class, Integer)</code>.
   * @throws Throwable                if an error occurs.
   */
  public static FunctionReference fun(Object name, Object module, Object arity, Object varargs) throws Throwable {
    require(name instanceof String, "name must be a String");
    require(module instanceof Class, "module must be a module (e.g., foo.bar.Some.module)");
    require(arity instanceof Integer, "name must be an Integer");
    require(varargs instanceof Boolean, "varargs must be a Boolean");
    final Class<?> moduleClass = (Class<?>) module;
    final String functionName = (String) name;
    final int functionArity = (Integer) arity;
    final boolean isVarargs = (Boolean) varargs;
    Method targetMethod = null;
    Predicate<Method> candidate = Extractors.matchFunctionReference(functionName, functionArity, isVarargs);
    final List<Method> validCandidates = Extractors.getMethods(moduleClass)
      .filter(candidate)
      .collect(toList());
    if (validCandidates.size() == 1) {
      targetMethod = validCandidates.get(0);
      // FIXME: only if the defining module is the calling module (see #319)
      targetMethod.setAccessible(true);
      return toFunctionReference(targetMethod, functionArity);
    }
    if (validCandidates.size() > 1) {
      // TODO: return the first method, printing a warning
      throw new AmbiguousFunctionReferenceException(("The reference to " + name + " in " + module
          + ((functionArity < 0) ? "" : (" with arity " + functionArity))
          + " is ambiguous"));
    }
    Optional<Method> target = getImportedFunctions(moduleClass).filter(candidate).findFirst();
    if (target.isPresent()) {
      return toFunctionReference(target.get(), functionArity);
    }
    throw new NoSuchMethodException((name + " in " + module
        + (functionArity < 0 ? "" : (" with arity " + functionArity))));
  }

  /**
   * Obtains the first reference to a function.
   * <p>
   * This is the same as calling {@code fun(name, module, arity, false)}.
   *
   * @see Predefined#fun(Object, Object, Object, Object)
   */
  public static FunctionReference fun(Object name, Object module, Object arity) throws Throwable {
    return fun(name, module, arity, false);
  }

  /**
   * Obtains the first reference to a function.
   * <p>
   * This is the same as calling {@code fun(name, module, -1)}.
   *
   * @see Predefined#fun(Object, Object, Object)
   */
  public static FunctionReference fun(Object name, Object module) throws Throwable {
    return fun(name, module, -1);
  }

  private static FunctionReference toFunctionReference(Method targetMethod, int functionArity) throws Throwable {
    String[] parameterNames = Arrays.stream(targetMethod.getParameters())
      .map(Parameter::getName)
      .toArray(String[]::new);
    if (isMethodDecorated(targetMethod)) {
      return new FunctionReference(getDecoratedMethodHandle(targetMethod, functionArity), parameterNames);
    }
    return new FunctionReference(MethodHandles.publicLookup().unreflect(targetMethod), parameterNames);
  }

  private static Stream<Method> getImportedFunctions(Class<?> source) {
    return Extractors.getImportedNames(source)
      .map(Loader.forClass(source))
      .filter(Objects::nonNull)
      .flatMap(Extractors::getMethods)
      .filter(Extractors::isPublic);
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
    Charset charset = null;
    if (encoding instanceof String) {
      charset = Charset.forName((String) encoding);
    } else if (encoding instanceof Charset) {
      charset = (Charset) encoding;
    } else {
      throw new IllegalArgumentException("encoding must be either a string or a charset instance");
    }
    return new String(Files.readAllBytes(pathFrom(file)), charset);
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
   * Writes some text to a file.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   */
  public static void textToFile(Object text, Object file) throws Throwable {
    textToFile(text, file, Charset.defaultCharset());
  }

  /**
   * Writes some text to a file using the given {@link Charset}.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   * @param charset the charset to encode the text in.
   */
  public static void textToFile(Object text, Object file, Object charset) throws Throwable {
    require(text instanceof String, "text must be a string");
    Charset encoding;
    if (charset instanceof String) {
      encoding = Charset.forName((String) charset);
    } else {
      require(charset instanceof Charset, "not a charset");
      encoding = (Charset) charset;
    }
    String str = (String) text;
    if ("-".equals(file.toString())) {
      System.out.write(str.getBytes(encoding));
    } else {
      Path path = pathFrom(file);
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.write(
          path,
          str.getBytes(encoding),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
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

  /**
   * Sleep on the current thread.
   *
   * @param ms time in milliseconds.
   * @throws InterruptedException in case the thread gets interrupted.
   */
  public static void sleep(long ms) throws InterruptedException {
    java.lang.Thread.sleep(ms);
  }

  /**
   * Return a universally unique identifier as String.
   *
   * @return a universally unique identifier as String
   */
  public static String uuid() {
    return java.util.UUID.randomUUID().toString();
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

  /**
   * Removes an element of a List by index.
   *
   * @param lst the list to remove the element from.
   * @param idx an Integer representing the index of the element to remove.
   * @return the element that was removed.
   * @throws IndexOutOfBoundsException if {@code idx}
   */
  public static Object removeByIndex(List<?> lst, Integer idx) {
    return lst.remove(idx.intValue());
  }

  /**
   * Returns a box containing the given object.
   * <p>
   * Useful whenever an explicit reference is needed, for instance to create a closure with internal
   * mutable state:
   * <pre>
   *    function counter = |init| {
   *      let current = box(init)
   *      return -> current: getAndSet(current: get() + 1)
   *    }
   *
   *    let c = counter(3)
   *    c() # 3
   *    c() # 4
   *    c() # 5
   * </pre>
   *
   * @param obj the object to reference.
   * @return a {@code java.util.concurrent.atomic.AtomicReference} instance wrapping the object
   */
  public static Object box(Object obj) {
    return new java.util.concurrent.atomic.AtomicReference<Object>(obj);
  }

  // ...................................................................................................................

  /**
   * Varargs version of a list constructor.
   *
   * @return a list of the given values.
   */
  public static List<Object> list(Object... values) {
    return new LinkedList<Object>(Arrays.asList(values));
  }

  /**
   * Varargs version of a set constructor.
   *
   * @return a set of the given values.
   */
  public static Set<Object> set(Object... values) {
    return new LinkedHashSet<Object>(Arrays.asList(values));
  }

  /**
   * array constructor.
   *
   * @return an array of the given values.
   */
  public static Object[] array(Object... values) {
    return values;
  }

  /**
   * Varargs version of a vector constructor.
   *
   * @return a vector of the give values.
   */
  public static List<Object> vector(Object... values) {
    return new ArrayList<Object>(Arrays.asList(values));
  }

  /**
   * Tuple constructor.
   *
   * @return a tuple of the given values.
   */
  public static Tuple tuple(Object... values) {
    return new Tuple(values);
  }

  /**
   * Varargs version of a map constructor.
   *
   * @param items tuples containing the key and the value.
   * @return a map corresponding to the given key/value pairs.
   */
  public static Map<Object, Object> map(Tuple... items) {
    Map<Object, Object> m = new LinkedHashMap<>();
    for (Tuple t : items) {
      m.put(t.get(0), t.get(1));
    }
    return m;
  }
}
