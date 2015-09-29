/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.utils;

public class NamingUtils {

  public static int packageClassSeparatorIndex(String moduleName) {
    if (moduleName != null) {
      return moduleName.lastIndexOf('.');
    }
    return -1;
  }

  public static String extractTargetJavaPackage(String moduleName) {
    int packageClassSeparatorIndex = packageClassSeparatorIndex(moduleName);
    if (packageClassSeparatorIndex > 0) {
      return moduleName.substring(0, packageClassSeparatorIndex);
    }
    return "";
  }

  public static String extractTargetJavaClass(String moduleName) {
    int packageClassSeparatorIndex = packageClassSeparatorIndex(moduleName);
    if (packageClassSeparatorIndex > 0) {
      return moduleName.substring(packageClassSeparatorIndex + 1);
    }
    return moduleName;
  }
}
