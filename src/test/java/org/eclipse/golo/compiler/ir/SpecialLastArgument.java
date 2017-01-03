 /*
* Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.golo.compiler.ir;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

public class SpecialLastArgument extends GoloTest {
  @Override
  protected String srcDir() {
    return "for-test/";
  }

  @Test
  public void testSpecialLastArg() throws Throwable {
    run("special-last-arg");
  }
}
