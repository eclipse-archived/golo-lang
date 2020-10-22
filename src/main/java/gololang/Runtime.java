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

import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import gololang.ir.GoloModule;

import java.io.InputStream;
import java.net.URL;

import static java.util.Objects.requireNonNull;
import static gololang.IO.toURL;

/**
 * A utility module containing functions to interact with the runtime environment.
 */
public final class Runtime {
  private Runtime() {
    // utility class
  }

  /**
   * Get a boolean value from a system property or an environment variable.
   *
   * <p>Equivalent to {@code loadBoolean("true", prop, env, defaultValue)}
   *
   * @param prop system property to get the value from, ignored if {@code null}.
   * @param env environment variable to get the value from, ignored if {@code null}.
   * @param defaultValue value to return if neither the property nor the environment are defined.
   */
  public static boolean loadBoolean(String prop, String env, boolean defaultValue) {
    return loadBoolean("true", prop, env, defaultValue);
  }

  /**
   * Get a boolean value from a system property or an environment variable.
   *
   * <p>Checks if the property is defined and equals to the value (case insensitive), otherwise checks the environment
   * variable, then use the default value.
   *
   * @param value content of the property.
   * @param prop system property to get the value from, ignored if {@code null}.
   * @param env environment variable to get the value from, ignored if {@code null}.
   * @param defaultValue value to return if neither the property nor the environment are defined.
   * @throws NullPointerException if {@code value} is null and one of the property or environment is defined.
   */
  public static boolean loadBoolean(String value, String prop, String env, boolean defaultValue) {
    String val = null;
    if (prop != null) {
      val = System.getProperty(prop);
    }
    if (val == null && env != null) {
      val = System.getenv(env);
    }
    if (val == null) {
      return defaultValue;
    }
    return val.toLowerCase().equals(value.toLowerCase());
  }

  private static boolean debug = loadBoolean("golo.debug", "GOLO_DEBUG", false);
  private static boolean showStackTrace = loadBoolean("golo.debug.trace", "GOLO_DEBUG_TRACE", true);

  /**
   * Checks if debug mode is active.
   *
   * <p>The initial value is false, but can be redefined using the {@code golo.debug} system property, or the
   * {@code GOLO_DEBUG} environment variable.
   */
  public static boolean debugMode() {
    return debug;
  }

  /**
   * Defines debug mode.
   */
  public static void debugMode(boolean v) {
    debug = v;
  }

  /**
   * Checks if the main golo command should print stack traces on error.
   */
  public static boolean showStackTrace() {
    return showStackTrace || debug;
  }

  /**
   * Defines if the main golo command should print stack traces on error.
   */
  public static void showStackTrace(boolean v) {
    showStackTrace = v;
  }

  private static CliCommand command;

  /**
   * Defines the golo command currently used.
   *
   * <p>This command is called at init time and should not be called in userland code.
   */
  public static void command(CliCommand cmd) {
    command = requireNonNull(cmd);
  }

  /**
   * Returns the golo command currently used.
   */
  public static CliCommand command() {
    return command;
  }

  /**
   * Returns the current thread class loader.
   * <p>
   * Possibly wrapped in a {@code GoloClassLoader} if necessary.
   */
  public static GoloClassLoader classLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl instanceof GoloClassLoader) {
      return (GoloClassLoader) cl;
    }
    return new GoloClassLoader(cl);
  }

  /**
   * Load a golo file given its path or URL.
   *
   * <p>Equivalent to {@code load(module, true)}.
   *
   * @param module can be a {@code String}, a {@code java.nio.file.Path}, a {@code java.net.URL} or a {@code java.net.URI} identifying the file to load, or directly a {@link GoloModule}.
   * @return the loaded module class.
   * @see #load(Object, boolean)
   */
  public static Class<?> load(Object module) throws Exception {
    return load(module, true);
  }

  /**
   * Load a golo file given its path or URL.
   *
   * <p>If {@code failOnException} is false, any exception is swallowed and null is return.
   *
   * @param module can be a {@code String}, a {@code java.nio.file.Path}, a {@code java.net.URL} or a {@code java.net.URI} identifying the file to load, or directly a {@link GoloModule}.
   * @param failOnException re-raise exception or not
   * @return the loaded module class, or {@code null} if an error occurred and {@code failOnException} is {@code false}
   * @see #load(Object, boolean)
   */
  public static Class<?> load(Object module, boolean failOnException) throws Exception {
    try {
      if (module instanceof GoloModule) {
        GoloModule mod = (GoloModule) module;
        return classLoader().load(mod);
      }
      URL url = toURL(module);
      try (InputStream is = url.openStream()) {
        return classLoader().load(module.toString(), is);
      }
    } catch (Exception e) {
      if (failOnException) {
        throw e;
      }
      return null;
    }
  }

  /**
   * Returns the current golo version.
   */
  public static String version() {
    return org.eclipse.golo.cli.command.Metadata.VERSION;
  }
}
