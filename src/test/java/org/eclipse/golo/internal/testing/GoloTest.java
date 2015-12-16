/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.internal.testing;

import org.testng.annotations.BeforeMethod;
import org.eclipse.golo.compiler.GoloClassLoader;

public abstract class GoloTest {
  private GoloClassLoader loader;
  private static final String RESOURCES = "src/test/resources/";

  protected void load(String moduleName) throws Throwable {
    TestUtils.compileAndLoadGoloModule(RESOURCES + srcDir(), moduleName + ".golo", loader);
  }

  protected void run(String moduleName) throws Throwable {
    TestUtils.runTests(RESOURCES + srcDir(), moduleName + ".golo", loader);
  }

  protected abstract String srcDir();

  @BeforeMethod
  public void setUp() {
    loader = TestUtils.classLoader(this);
  }

}
