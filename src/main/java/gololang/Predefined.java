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

import org.eclipse.golo.runtime.AmbiguousFunctionReferenceException;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.objectweb.asm.Type;
import org.eclipse.golo.runtime.Extractors;
import org.eclipse.golo.runtime.Loader;
import org.eclipse.golo.runtime.WithCaller;
import gololang.ir.GoloElement;
import gololang.ir.GoloFunction;
import org.eclipse.golo.compiler.macro.Macro;

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
   *
   * @deprecated Since 3.3, use {@link gololang.IO#readln()}. Will be removed in 4.0
   */
  @Deprecated
  public static String readln() throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.readln();
  }

  /**
   * Reads the next line of characters from the console.
   *
   * @param message displays a prompt message.
   * @return a String.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#readln(String)}. Will be removed in 4.0
   */
  @Deprecated
  public static String readln(String message) throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.readln(message);
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @return a String.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#readPassword()}. Will be removed in 4.0
   */
  @Deprecated
  public static String readPassword() throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.readPassword();
  }

  /**
   * Reads a password from the console with echoing disabled.
   *
   * @param message displays a prompt message.
   * @return a String.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#readPassword(String)}. Will be removed in 4.0
   */
  @Deprecated
  public static String readPassword(String message) throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.readPassword(message);
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @return a character array.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#secureReadPassword()}. Will be removed in 4.0
   */
  @Deprecated
  public static char[] secureReadPassword() throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.secureReadPassword();
  }

  /**
   * Reads a password from the console with echoing disabled, returning an {@code char[]} array.
   *
   * @param message displays a prompt message.
   * @return a character array.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#secureReadPassword(String)}. Will be removed in 4.0
   */
  @Deprecated
  public static char[] secureReadPassword(String message) throws IOException {
    // TODO: remove in 4.0
    return gololang.IO.secureReadPassword(message);
  }

  // ...................................................................................................................
  /**
   * Requires that an object is not the <code>null</code> reference.
   *
   * @param obj the object to test against <code>null</code>.
   * @return the object itself if not null
   * @throws AssertionError if <code>obj</code> is <code>null</code>.
   */
  public static Object requireNotNull(Object obj) throws AssertionError {
    if (obj != null) {
      return obj;
    }
    throw new AssertionError("null reference encountered");
  }

  /**
   * Requires that an object has the given type.
   *
   * @param obj the object to test
   * @param type the expected type of the object
   * @return the object itself if not null
   * @throws AssertionError if {@code obj} is not a {@code type}
   */
  public static <T> T requireType(Object obj, Class<? extends T> type) {
    if (type.isInstance(obj)) {
      return type.cast(obj);
    }
    throw new AssertionError(String.format("%s expected, got %s", type.getName(), obj.getClass().getName()));
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
  private static void checkRangeTypes(Object value, String name) {
    require((value instanceof Integer)
        || (value instanceof Long)
        || (value instanceof Character)
        || (value instanceof BigInteger),
        name + " must either be an Integer, Long, Character or BigInteger");
  }

  /**
   * Makes an range object between two bounds. Range objects implement
   * <code>java.lang.Collection</code> (immutable), so they can be used in Golo <code>foreach</code>
   * loops.
   *
   * @param from the lower-bound (inclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
   * @param to   the upper-bound (exclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
   * @return a range object.
   * @see java.util.Collection
   */
  public static Object range(Object from, Object to) {
    checkRangeTypes(from, "from");
    checkRangeTypes(to, "to");
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
    if (from instanceof BigInteger || to instanceof BigInteger) {
      return new BigIntegerRange(bigIntegerValue(from), bigIntegerValue(to));
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
   * @param to the upper-bound (exclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
   * @return a range object.
   * @see gololang.Predefined#range
   */
  public static Object range(Object to) {
    checkRangeTypes(to, "to");
    if (to instanceof Integer) {
      return new IntRange((Integer) to);
    }
    if (to instanceof Long) {
      return new LongRange((Long) to);
    }
    if (to instanceof BigInteger) {
      return new BigIntegerRange((BigInteger) to);
    }
    return new CharRange((Character) to);
  }

  /**
   * Makes an decreasing range object between two bounds. Range objects implement <code>java.lang.Collection</code>, so
   * they can be used in Golo <code>foreach</code> loops.
   *
   * @param from the upper-bound (inclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
   * @param to   the lower-bound (exclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
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
   * @param from the upper-bound (inclusive) as an {@code Integer}, {@code Long}, {@code Character} or {@code BigInteger}.
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
  @WithCaller
  public static FunctionReference fun(Class<?> caller, Object name, Object module, Object arity, Object varargs) throws Throwable {
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
      if (module == caller || caller == null) {
        targetMethod.setAccessible(true);
      }
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
   * @see #fun(Class, Object, Object, Object, Object)
   */
  @WithCaller
  public static FunctionReference fun(Class<?> caller, Object name, Object module, Object arity) throws Throwable {
    return fun(caller, name, module, arity, false);
  }

  /**
   * Obtains the first reference to a function.
   * <p>
   * This is the same as calling {@code fun(name, module, -1)}.
   *
   * @see #fun(Class, Object, Object, Object)
   */
  @WithCaller
  public static FunctionReference fun(Class<?> caller, Object name, Object module) throws Throwable {
    return fun(caller, name, module, -1);
  }

  public static FunctionReference toFunctionReference(Method targetMethod, int functionArity) throws Throwable {
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
   *
   * @deprecated Since 3.3, use {@link gololang.IO#fileToText(Object, Object)}. Will be removed in 4.0
   */
  @Deprecated
  public static Object fileToText(Object file, Object encoding) throws Throwable {
    // TODO: remove in 4.0
    return gololang.IO.fileToText(file, encoding);
  }

  /**
   * Writes some text to a file.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   *
   * @deprecated Since 3.3, use {@link gololang.IO#textToFile(Object, Object)}. Will be removed in 4.0
   */
  @Deprecated
  public static void textToFile(Object text, Object file) throws Throwable {
    // TODO: remove in 4.0
    gololang.IO.textToFile(text, file);
  }

  /**
   * Writes some text to a file using the given {@link Charset}.
   *
   * The file and parents directories are created if they does not exist. The file is overwritten if it already exists. If the file is {@code "-"}, the content is written to standard output.
   *
   * @param text the text to write.
   * @param file the file to write to as an instance of either {@link String}, {@link File} or {@link Path}.
   * @param charset the charset to encode the text in.
   * @deprecated Since 3.3, use {@link gololang.IO#textToFile(Object, Object, Object)}. Will be removed in 4.0
   */
  @Deprecated
  public static void textToFile(Object text, Object file, Object charset) throws Throwable {
    // TODO: remove in 4.0
    gololang.IO.textToFile(text, file, charset);
  }

  /**
   * Check if a file exists.
   *
   * @param file the file to read from as an instance of either {@link String}, {@link File} or {@link Path}.
   * @return true if the file exists, false if it doesn't
   *
   * @deprecated Since 3.3, use {@link gololang.IO#fileExists(Object)}. Will be removed in 4.0
   */
  @Deprecated
  public static boolean fileExists(Object file) {
    // TODO: remove in 4.0
    return gololang.IO.fileExists(file);
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
    return Class.forName("[" + intertnalDescriptorFormClass(type), true, type.getClassLoader());
  }

  private static String intertnalDescriptorFormClass(Class<?> clazz) {
    return Type.getDescriptor(clazz).replaceAll("/", ".");
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
    if (obj instanceof Number) {
      return (char) ((Number) obj).doubleValue();
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
    if (obj instanceof Number) {
      return ((Number) obj).intValue();
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
    if (obj instanceof Number) {
      return ((Number) obj).longValue();
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
    if (obj instanceof Number) {
      return ((Number) obj).doubleValue();
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
    if (obj instanceof Number) {
      return ((Number) obj).floatValue();
    }
    if (obj instanceof String) {
      return Float.valueOf((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }
  // END GENERATED .....................................................................................................

  /**
   * Gives the BigDecimal value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Float value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static BigDecimal bigDecimalValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof BigDecimal) {
      return (BigDecimal) obj;
    }
    if (obj instanceof BigInteger) {
      return new BigDecimal((BigInteger) obj);
    }
    if (obj instanceof Number) {
      return BigDecimal.valueOf(((Number) obj).doubleValue());
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return BigDecimal.valueOf((long) value);
    }
    if (obj instanceof String) {
      return new BigDecimal((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  /**
   * Gives the BigInteger value of some number or String object.
   *
   * @param obj a boxed number or String value.
   * @return the Float value.
   * @throws IllegalArgumentException if {@code obj} is not a number or a String.
   */
  public static BigInteger bigIntegerValue(Object obj) throws IllegalArgumentException {
    if (obj instanceof BigInteger) {
      return (BigInteger) obj;
    }
    if (obj instanceof BigDecimal) {
      return ((BigDecimal) obj).toBigInteger();
    }
    if (obj instanceof Number) {
      return BigInteger.valueOf(((Number) obj).longValue());
    }
    if (obj instanceof Character) {
      char value = (Character) obj;
      return BigInteger.valueOf((long) value);
    }
    if (obj instanceof String) {
      return new BigInteger((String) obj);
    }
    throw new IllegalArgumentException("Expected a number or a string, but got: " + obj);
  }

  // ...................................................................................................................
  /**
   * Create a {@code String} by concatenating all arguments.
   * <p>
   * For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let s = str("The answer", " is ", 2 * 21)
   * </code></pre>
   * This is functionally equivalent to:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let s = ["The answer", " is ", 2 * 21]: join("")
   * </code></pre>
   */
  public static String str(Object... args) {
    if (args == null || args.length == 0) {
      return "";
    }
    if (args.length == 1) {
      return String.valueOf(args[0]);
    }
    StringBuilder sb = new StringBuilder();
    for (Object o : args) {
      sb.append(o);
    }
    return sb.toString();
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
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   *    function counter = |init| {
   *      let current = box(init)
   *      return -> current: getAndSet(current: get() + 1)
   *    }
   *
   *    let c = counter(3)
   *    c() # 3
   *    c() # 4
   *    c() # 5
   * </code></pre>
   *
   * @param obj the object to reference.
   * @return a {@code java.util.concurrent.atomic.AtomicReference} instance wrapping the object
   */
  public static Object box(Object obj) {
    return new java.util.concurrent.atomic.AtomicReference<>(obj);
  }

  // ...................................................................................................................
  /**
   * Varargs version of a list constructor.
   *
   * @return a list of the given values.
   */
  public static List<Object> list(Object... values) {
    return new LinkedList<>(Arrays.asList(values));
  }

  /**
   * Varargs version of a set constructor.
   *
   * @return a set of the given values.
   */
  public static Set<Object> set(Object... values) {
    return new LinkedHashSet<>(Arrays.asList(values));
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
    return new ArrayList<>(Arrays.asList(values));
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

  /**
   * Macro to define a special macro.
   * <p>
   * A special macro is a macro whose first implicit parameter is the expansion visitor itself.
   * <p>
   * For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * &#64;special
   * macro mySpecialMacro = |visitor, arg| {
   *   ...
   * }
   * </code></pre>
   * <p>See also the <a href="../../golo-guide.html#special_macros">Golo Guide</a>
   */
  @Macro
  public static GoloElement<?> special(GoloFunction fun) {
    if (!fun.isMacro()) {
      throw new IllegalArgumentException("The `special` macro decorator must be used on macros");
    }
    if (fun.getArity() == 0 || (fun.isContextualMacro() && fun.getArity() == 1)) {
      throw new IllegalArgumentException(String.format(
            "Special macro `%s` must take at least 1 argument (`expander` would be a good idea…)",
            fun.getName()));
    }
    return fun.special(true);
  }

  /**
   * Macro to define a contextual macro.
   * <p>
   * A contextual macro is a macro whose first implicit parameter is the macro call itself.
   * <p>
   * For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * &#64;contextual
   * macro mySpecialMacro = |self, arg| {
   *   ...
   * }
   * </code></pre>
   *
   * <p>See also the <a href="../../golo-guide.html#contextual_macros">Golo Guide</a>
   */
  @Macro
  public static GoloElement<?> contextual(GoloFunction fun) {
    if (!fun.isMacro()) {
      throw new IllegalArgumentException("The `contextual` macro decorator must be used on macros");
    }
    if (fun.getArity() == 0 || (fun.isSpecialMacro() && fun.getArity() == 1)) {
      throw new IllegalArgumentException(String.format(
            "Contextual macro `%s` must take at least 1 argument (`self` would be a good idea…)",
            fun.getName()));
    }
    return fun.contextual(true);
  }
}
