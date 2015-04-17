/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.internal.testing;

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
