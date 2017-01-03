/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.internal.testing;

import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.parser.ParseException;
import org.testng.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isPublic;
import static org.testng.Assert.fail;

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
    return compileAndLoadGoloModule(sourceFolder, goloFile, new GoloClassLoader(TestUtils.class.getClassLoader()));
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile, GoloClassLoader goloClassLoader) throws IOException, ParseException, ClassNotFoundException {
    try {
      return goloClassLoader.load(goloFile, new FileInputStream(sourceFolder + goloFile));
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
        && (m.getName().startsWith("test_") || m.getName().startsWith("check_"))
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
    return new GoloClassLoader(o.getClass().getClassLoader());
  }

  public static void runTests(String sourceFolder, String goloModuleName, GoloClassLoader classLoader) throws Throwable {
    Class<?> testModule = compileAndLoadGoloModule(sourceFolder, goloModuleName, classLoader);
    for (Method testMethod : getTestMethods(testModule)) {
      try {
        testMethod.invoke(null);
      } catch (InvocationTargetException e) {
        fail("method " + testMethod.getName()
            + " in " + sourceFolder + goloModuleName + " failed: "
            + e.getCause());
      }
    }
  }

}
