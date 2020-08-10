/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

public class TceTest extends GoloTest {

  @Override
  public String srcDir() {
    return "for-parsing-and-compilation/";
  }

  @Test
  public void testTce() throws Throwable {
    run("tce");
  }

}
