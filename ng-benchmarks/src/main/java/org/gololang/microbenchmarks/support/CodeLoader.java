package org.gololang.microbenchmarks.support;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CodeLoader {

  public static final Class<?>[] NO_ARGS = {};

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public MethodHandle golo(String file, String func, int argCount) {
    GoloClassLoader classLoader = new GoloClassLoader();
    String filename = "snippets/golo/" + file + ".golo";
    Class<?> module = classLoader.load(filename, CodeLoader.class.getResourceAsStream("/" + filename));
    try {
      return LOOKUP.findStatic(module, func, MethodType.genericMethodType(argCount));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
