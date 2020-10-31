/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.IParameterSplitter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Arrays;

import org.eclipse.golo.compiler.GoloClassLoader;

/**
 * Manage the classpath and initialize the class loader.
 */
@Parameters(resourceBundle = "commands")
public class ClasspathOption {

  /**
   * The system property to define the path.
   */
  public static final String PROPERTY = "golo.class.path";

  /**
   * The environment variable used to define the path.
   */
  public static final String ENV = "GOLOPATH";
  private static final List<String> DEFAULT = Collections.singletonList(".");
  private static final String SEP = System.getProperty("path.separator", ":");

  private static final class ClasspathSplitter implements IParameterSplitter {
    private static final String RE = String.format("[,%s]", SEP);
    public List<String> split(String value) {
      return Arrays.asList(value.split(RE));
    }
  }

  @Parameter(names = "--classpath", variableArity = true, descriptionKey = "classpath", splitter = ClasspathSplitter.class)
  List<String> classpath = new LinkedList<>();

  private static URLClassLoader primaryClassLoader(List<String> classpath) throws MalformedURLException {
    URL[] urls = new URL[classpath.size()];
    for (int i = 0; i < classpath.size(); i++) {
      urls[i] = new File(classpath.get(i)).toURI().toURL();
    }
    return new URLClassLoader(urls);
  }

  /**
   * Create a golo classloader using the given classpath list.
   * <p>
   * If {@code classpath} is empty, packages from the {@code PROPERTY} system property are used, then from
   * {@code ENV} environment variable if no property is defined. Finally, the current directory is used if no other path
   * is defined.
   * The current thread class loader is set to the created golo class loader.
   *
   * @param classpath a list of string representing classpath elements
   * @return the corresponding class loader
   */
  public static GoloClassLoader initGoloClassLoader(List<String> classpath) throws MalformedURLException {
    URLClassLoader primaryClassLoader = primaryClassLoader(initClassPath(classpath));
    GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
    Thread.currentThread().setContextClassLoader(loader);
    return loader;
  }

  /**
   * Init the class loader using the parsed command line option.
   */
  public GoloClassLoader initGoloClassLoader() throws MalformedURLException {
    return initGoloClassLoader(this.classpath);
  }

  private static List<String> initClassPath(List<String> init) {
    // priority CLI > property > env > default
    List<String> classpath = init;
    if (classpath.isEmpty()) {
      classpath = getFromEnv(System.getProperty(PROPERTY));
    }
    if (classpath.isEmpty()) {
      classpath = getFromEnv(System.getenv(ENV));
    }
    if (classpath.isEmpty()) {
      classpath = DEFAULT;
    }
    System.setProperty(PROPERTY, String.join(SEP, classpath));
    return classpath;
  }

  private static List<String> getFromEnv(String value) {
    if (value == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(value.split(SEP));
  }
}
