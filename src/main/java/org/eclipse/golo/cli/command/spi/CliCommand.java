/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli.command.spi;

import org.eclipse.golo.compiler.GoloCompilationException;
import gololang.Messages;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;


public interface CliCommand {

  class NoMainMethodException extends NoSuchMethodException {
  }

  // NOT DOCUMENTED
  boolean DEBUG = Boolean.valueOf(System.getProperty("golo.debug", "false"));
  boolean SHOW_TRACE = Boolean.valueOf(System.getProperty("golo.debug.trace", "true"));

  void execute() throws Throwable;

  default URLClassLoader primaryClassLoader(List<String> classpath) throws MalformedURLException {
    URL[] urls = new URL[classpath.size()];
    int index = 0;
    for (String element : classpath) {
      urls[index] = new File(element).toURI().toURL();
      index = index + 1;
    }
    return new URLClassLoader(urls);
  }

  default void callRun(Class<?> klass, String[] arguments) throws Throwable {
    MethodHandle main;
    try {
      main = publicLookup().findStatic(klass, "main", methodType(void.class, String[].class));
    } catch (NoSuchMethodException e) {
      throw new NoMainMethodException().initCause(e);
    }
    main.invoke(arguments);
  }

  default void handleCompilationException(GoloCompilationException e) {
    handleCompilationException(e, true);
  }

  default void handleCompilationException(GoloCompilationException e, boolean exit) {
    handleThrowable(e, false);
    for (GoloCompilationException.Problem problem : e.getProblems()) {
      Messages.error(problem.getDescription());
    }
    if (exit) {
      System.exit(1);
    }
  }

  default void handleThrowable(Throwable e) {
    handleThrowable(e, true);
  }

  default void handleThrowable(Throwable e, boolean exit) {
    handleThrowable(e, exit,  DEBUG || SHOW_TRACE);
  }

  default void handleThrowable(Throwable e, boolean exit, boolean withStack) {
    Messages.error(e);
    if (e.getCause() != null) {
      Messages.error(e.getCause().getMessage());
    }
    if (withStack) {
      Messages.printStackTrace(e);
    }
    if (exit) {
      System.exit(1);
    }
  }
}
