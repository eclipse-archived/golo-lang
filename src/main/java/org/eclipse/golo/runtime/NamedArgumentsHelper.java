/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class NamedArgumentsHelper {

  private NamedArgumentsHelper() {
    // utility class
  }

  public static Boolean hasNamedParameters(Method method) {
    return Arrays.stream(method.getParameters()).allMatch(Parameter::isNamePresent);
  }

  public static List<String> getParameterNames(Method method) {
    return Arrays.stream(method.getParameters())
        .map(Parameter::getName)
        .collect(toList());
  }

  public static void checkArgumentPosition(int position, String argument, String declaration) {
    if (position == -1) {
      throw new IllegalArgumentException(
          "Argument name " + argument
          + " not in parameter names used in declaration: " + declaration);
    }
  }
}
