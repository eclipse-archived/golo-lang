/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.internal.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.fail;

public abstract class GoloTest {
  private GoloClassLoader loader;
  private static final String RESOURCES = "src/test/resources/";

  public Class<?> load(String moduleName) throws Throwable {
    return TestUtils.compileAndLoadGoloModule(RESOURCES + srcDir(), moduleName + ".golo", loader);
  }

  public void run(String moduleName) throws Throwable {
    TestUtils.runTestsIn(load(moduleName), filenameFor(moduleName));
  }

  protected Object run(String moduleName, String functionName, Object... args) throws Throwable {
    Class<?> module = load(moduleName);
    Method method = module.getMethod(functionName);
    try {
      return method.invoke(null, args);
    } catch (InvocationTargetException e) {
      fail(String.format("function %s in %s%s%s.golo failed: %s", functionName, RESOURCES, srcDir(), moduleName, e.getCause()));
    }
    return null;
  }

  public String filenameFor(String moduleName) {
    return RESOURCES + srcDir() + moduleName + ".golo";
  }

  public abstract String srcDir();

  @BeforeMethod
  public void setUp() {
    loader = TestUtils.classLoader(this);
  }

}
