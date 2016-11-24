/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli.command.spi;

import org.eclipse.golo.compiler.GoloCompilationException;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

public interface CliCommand {

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
    MethodHandle main = publicLookup().findStatic(klass, "main", methodType(void.class, String[].class));
    main.invoke(arguments);
  }

  default void handleCompilationException(GoloCompilationException e) {
    handleCompilationException(e, true);
  }

  default void handleCompilationException(GoloCompilationException e, boolean exit) {
    handleThrowable(e, false);
    for (GoloCompilationException.Problem problem : e.getProblems()) {
      System.out.println("[error] " + problem.getDescription());
    }
    if (exit) {
      System.exit(1);
    }
  }

  default void handleThrowable(Throwable e) {
    handleThrowable(e, true);
  }

  default void handleThrowable(Throwable e, boolean exit) {
    if (e.getMessage() != null) {
      System.out.println("[error] " + e.getMessage());
    }
    if (e.getCause() != null) {
      System.out.println("[error] " + e.getCause().getMessage());
    }
    if ("yes".equals(System.getenv("GOLO_DEBUG"))) {
      e.printStackTrace();
    }
    if (exit) {
      System.exit(1);
    }
  }
}
