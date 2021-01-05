/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.testing.support;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassWithOverloadedMethods {

  public String foo(String str) {
    return "# " + str;
  }

  public String foo(int i) {
    return "% " + i;
  }

  public String bar(String str, int i) {
    return str + " @" + i;
  }

  public String bar(int i, long j) {
    return i + " :: " + j;
  }

  public String baz(String a, String b, String c, String d, String e, String f, String g, String h) {
    return Stream.of(a, b, c, d, e, f, g, h).collect(Collectors.joining(" ^ "));
  }

  public String baz(String a, String b, String c, String d, String e, String f, int g, int h) {
    return Stream.of(a, b, c, d, e, f, g, h)
        .map(Object::toString)
        .collect(Collectors.joining(" ~ "));
  }
}
