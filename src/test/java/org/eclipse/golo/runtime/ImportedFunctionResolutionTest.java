/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

public class ImportedFunctionResolutionTest extends GoloTest {

  private static final String[] MODS = {
    "v0",
    "v1b",
    "v1",
    "v2b",
    "v2c",
    "v2",
    "v2inv",
    "v3-1-1",
    "v3-1-2",
    "v3-1-3",
    "v3-2-1",
    "v3-2-2",
    "v3-3"
  };


  @Override
  protected String srcDir() {
    return "for-execution/call-resolution/";
  }

  @Test
  public void with_package() throws Throwable {
    load("foo");
    load("bar");
    load("plop");
    for (String m : MODS) {
      run(m);
    }
    run("v4");
  }

  @Test
  public void without_package() throws Throwable {
    load("foo");
    load("bar");
    for (String m : MODS) {
      run(m);
    }
  }
}

