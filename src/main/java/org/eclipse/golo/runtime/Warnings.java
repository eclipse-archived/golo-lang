/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.util.Arrays;

import static gololang.Messages.warning;
import static gololang.Messages.message;
import static org.eclipse.golo.cli.command.Metadata.GUIDE_BASE;

/**
 * A static class to deal with several kinds of warnings.
 * <p>
 */
public final class Warnings {
  private Warnings() {
    // utility class
  }

  private static final boolean NO_PARAMETER_NAMES = load("golo.warnings.no-parameter-names", "true");
  private static final boolean UNAVAILABLE_CLASS = load("golo.warnings.unavailable-class", "false");

  private static boolean load(String property, String def) {
    return Boolean.valueOf(System.getProperty(property, def));
  }

  public static void noParameterNames(String methodName, String[] argumentNames) {
    if (NO_PARAMETER_NAMES) {
      warning(message("no_parameter_names", methodName, Arrays.toString(argumentNames), GUIDE_BASE));
    }
  }

  public static void unavailableClass(String className, String callerModule) {
    if (UNAVAILABLE_CLASS && !className.startsWith("java.lang") && !className.startsWith("gololang")) {
      warning(message("unavailable_class", className, callerModule, GUIDE_BASE));
    }
  }
}
