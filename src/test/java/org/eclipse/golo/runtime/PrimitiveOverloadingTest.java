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

public class PrimitiveOverloadingTest extends GoloTest {

  public static class Lib {

    public String onInt(int i) {
      return "int";
    }

    public String onLong(long l) {
      return "long";
    }

    public String onFloat(float f) {
      return "float";
    }

    public String onDouble(double d) {
      return "double";
    }

    public String overloaded(float a, int b) {
      return "floatxint";
    }

    public String overloaded(int a, float b) {
      return "intxfloat";
    }

    public String overloaded2(int a, int b) {
      return "intxint";
    }

    public String overloaded2(float a, float b) {
      return "floatxfloat";
    }

    public String overloaded(int a) {
      return "int";
    }

    public String overloaded(float a) {
      return "float";
    }

    public static String soverloaded(int a) {
      return "int";
    }

    public static String soverloaded(float a) {
      return "float";
    }

    public static String soverloaded2(int a, int b) {
      return "intxint";
    }

    public static String soverloaded2(float a, float b) {
      return "floatxfloat";
    }

    public static String soverloaded(float a, int b) {
      return "floatxint";
    }

    public static String soverloaded(int a, float b) {
      return "intxfloat";
    }

    public static long getLong() {
      return 42L;
    }

    public static int getInt() {
      return 42;
    }

    public static float getFloat() {
      return 42.0f;
    }

    public static double getDouble() {
      return 42.0d;
    }
  }

  @Override
  protected String srcDir() {
    return "for-execution/";
  }

  @Test
  public void primitiveOverloading() throws Throwable {
    run("primitive-overloading");
  }
}
