/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.internal.testing;

import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.parser.ParseException;
import org.testng.Reporter;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import gololang.Tuple;


import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isPublic;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;


public class TestUtils {

  public static Iterator<Object[]> goloFilesIn(String path) {
    List<Object[]> data = new LinkedList<>();
    File[] files = new File(path).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".golo");
      }
    });
    for (File file : files) {
      data.add(new Object[]{file});
    }
    return data.iterator();
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile) throws IOException, ParseException, ClassNotFoundException {
    return compileAndLoadGoloModule(sourceFolder, goloFile, classLoader(TestUtils.class));
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile, GoloClassLoader goloClassLoader) throws IOException, ParseException, ClassNotFoundException {
    try {
      return goloClassLoader.load(goloFile, new FileReader(sourceFolder + goloFile));
    } catch (GoloCompilationException e) {
      for (GoloCompilationException.Problem p : e.getProblems()) {
        Reporter.log("In " + goloFile + ": " + p.getDescription(), shouldTestNgReportToConsole());
      }
      throw e;
    }
  }

  private static boolean shouldTestNgReportToConsole() {
    return Boolean.valueOf(System.getProperty("testng-report-to-console", "false"));
  }

  public static boolean isTestMethod(Method m) {
    return (isPublic(m.getModifiers())
        && isStatic(m.getModifiers())
        && (m.getName().startsWith("test") || m.getName().startsWith("check"))
        && m.getParameterCount() == 0);
  }

  public static Iterable<Method> getTestMethods(Class<?> module) {
    List<Method> methods = new LinkedList<>();
    for (Method m : module.getDeclaredMethods()) {
      if (isTestMethod(m)) {
        methods.add(m);
      }
    }
    return methods;
  }

  public static GoloClassLoader classLoader(Object o) {
    Class<?> cls;
    if (o instanceof Class<?>) {
      cls = (Class<?>) o;
    } else {
      cls = o.getClass();
    }
    return new GoloClassLoader(cls.getClassLoader());
  }

  public static void runTestMethod(Method testMethod, String filename) throws Throwable {
    try {
      Tuple result = (Tuple) testMethod.invoke(null);
      if (result != null) {
        assertEquals(result.get(0), result.get(1));
      }
    } catch (InvocationTargetException e) {
      fail(String.format("method %s in %s failed: %s",
            testMethod.getName(),
            filename,
            e.getCause()));
    }
  }

  public static void runTestMethod(Class<?> module, String methodName, String filename) throws Throwable {
    runTestMethod(module.getMethod(methodName), filename);
  }

  public static void runTestsIn(Class<?> testModule, String filename) throws Throwable {
    for (Method testMethod : getTestMethods(testModule)) {
      runTestMethod(testMethod, filename);
    }
  }

  public static void runTests(String sourceFolder, String goloModuleName, GoloClassLoader classLoader) throws Throwable {
    runTestsIn(
        compileAndLoadGoloModule(sourceFolder, goloModuleName, classLoader),
        sourceFolder + goloModuleName);
  }
}
