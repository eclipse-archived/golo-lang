/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

import java.util.function.*;


public class OperatorsAndHofTest extends GoloTest {

  @Override
  protected String srcDir() {
    return "for-test/";
  }

  @Test
  public void testFunctools() throws Throwable {
    run("functions");
  }

}
