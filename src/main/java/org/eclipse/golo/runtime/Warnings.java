/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.io.PrintStream;

import static java.util.Arrays.asList;

/**
 * A static class to deal with several kinds of warnings.
 * <p>
 */
public final class Warnings {
  private Warnings() {
    // utility class
  }

  private static final String GUIDE_BASE = "http://golo-lang.org/documentation/next/";
  private static final boolean NO_PARAMETER_NAMES = Boolean.valueOf(System.getProperty("golo.warnings.no-parameter-names", "true"));
  private static final boolean UNAVAILABLE_CLASS = Boolean.valueOf(System.getProperty("golo.warnings.unavailable-class", "true"));
  private static PrintStream out = System.err;

  public static void noParameterNames(String methodName, String[] argumentNames) {
    if (NO_PARAMETER_NAMES) {
      out.format("[warning] the function `%s` has no parameter names but is called with %s as argument names%n",
          methodName, asList(argumentNames));
      out.format("          see %s#warning-no-parameter-names for more information.%n", GUIDE_BASE);
    }
  }

  public static void unavailableClass(String className, String callerModule) {
    if (UNAVAILABLE_CLASS && !className.startsWith("java.lang") && !className.startsWith("gololang")) {
      out.format("[warning] `%s` used in `%s` can't be loaded.%n  See %s#warning-unavailable-class for more information.%n",
          className, callerModule, GUIDE_BASE);
    }
  }
}
