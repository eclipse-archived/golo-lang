/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.internal.testing;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class Tracing {

  public static final boolean shouldTrace = System.getProperty("golo.test.trace", "no").equals("yes");

  public static void print(Object obj) {
    System.out.print(obj);
  }

  public static void println() {
    System.out.println();
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }

  public static void traceBytecode(byte[] bytecode) {
    ClassReader reader = new ClassReader(bytecode);
    TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
    reader.accept(tracer, 0);
  }
}
