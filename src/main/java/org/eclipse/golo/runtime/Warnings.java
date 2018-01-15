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

import java.util.Arrays;
import java.util.HashSet;
import gololang.Tuple;

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
  private static final boolean DEPRECATED = load("golo.warnings.deprecated", "true");
  private static final HashSet<Tuple> SEEN_DEPRECATIONS = new HashSet<>();

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

  public static void deprecatedElement(String object, String caller) {
    if (DEPRECATED) {
      Tuple seen = new Tuple(object, caller);
      if (!SEEN_DEPRECATIONS.contains(seen)) {
        SEEN_DEPRECATIONS.add(seen);
        warning(message("deprecated_element", object, caller, GUIDE_BASE));
      }
    }
  }
}
