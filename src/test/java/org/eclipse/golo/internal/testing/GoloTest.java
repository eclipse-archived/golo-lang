/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

  protected Class<?> load(String moduleName) throws Throwable {
    return TestUtils.compileAndLoadGoloModule(RESOURCES + srcDir(), moduleName + ".golo", loader);
  }

  protected void run(String moduleName) throws Throwable {
    TestUtils.runTests(RESOURCES + srcDir(), moduleName + ".golo", loader);
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

  protected abstract String srcDir();

  @BeforeMethod
  public void setUp() {
    loader = TestUtils.classLoader(this);
  }

}
