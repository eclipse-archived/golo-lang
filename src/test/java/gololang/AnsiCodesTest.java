/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AnsiCodesTest {

  private static final String SRC = "src/test/resources/for-test/";
  private Class<?> moduleClass;

  private StringOutputStream out;

  public static class StringOutputStream extends OutputStream {

    private StringBuilder buffer = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
      buffer.append((char)b);
    }

    public String getString() {
      return buffer.toString();
    }
  }

  @BeforeMethod
  public void load_module() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    moduleClass = compileAndLoadGoloModule(SRC, "ansicodes.golo");
    out = new StringOutputStream();
    System.setOut(new PrintStream(out));
  }

  @Test
  public void cursor_movement() throws Throwable {
    Method cursor_movement = moduleClass.getMethod("cursor_movement");
    cursor_movement.invoke(null);
    assertThat(out.getString(), is("\u001B[2C\u001B[10A\u001B[5D\u001B[3B"));
  }
}
