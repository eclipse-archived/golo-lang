/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.meta;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

public class AnnotationsTest extends GoloTest {

  @Override
  public String srcDir() {
    return "for-macros/";
  }

  @Test
  public void tests() throws Throwable {
    load("annotation-macros");
    load("Annotate");
    run("annotation");
  }
}

