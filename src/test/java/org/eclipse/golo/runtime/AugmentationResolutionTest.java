/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.eclipse.golo.compiler.GoloClassLoader;

import static org.eclipse.golo.internal.testing.TestUtils.runTests;
import static org.eclipse.golo.internal.testing.TestUtils.classLoader;
import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;

public class AugmentationResolutionTest {
  private static final String SRC = "src/test/resources/for-execution/call-resolution/";
  private GoloClassLoader loader;

  @BeforeMethod
  public void setUp() {
    loader = classLoader(this);
  }

  @Test
  public void callStackLookup() throws Throwable {
    compileAndLoadGoloModule(SRC, "data.golo", loader);
    compileAndLoadGoloModule(SRC, "lib.golo", loader);
    runTests(SRC, "call-stack-lookup.golo", loader);
  }
}
