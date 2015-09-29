/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.testing.support;

public class GoloTestHelperFunctions {

  public static String concatenate(String first, String second) {
    return first + second;
  }

  public static boolean is(Object a, Object b) {
    return a == b;
  }
}
