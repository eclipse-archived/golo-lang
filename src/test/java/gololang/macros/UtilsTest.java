/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.macros;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

import java.util.function.*;


public class UtilsTest extends GoloTest {

  @Override
  public String srcDir() {
    return "for-test/";
  }

  @Test
  public void test() throws Throwable {
    run("macro-utils");
  }

}
