/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.util.function.Function;

/**
 * Encapsulate a {@code ClassLoader} to ease streaming.
 */
public final class Loader implements Function<String, Class<?>> {
  private final ClassLoader loader;

  Loader(ClassLoader loader) {
    this.loader = loader;
  }

  static Loader forClass(Class<?> klass) {
    return new Loader(klass.getClassLoader());
  }

  /**
   * Load the given class.
   * <p>
   * This only delegates to the underlying class loader, but returns {@code null} instead
   * of throwing an exception.
   *
   * @return the class if load succeed, {@code null} otherwise.
   */
  public Class<?> load(String name) {
    try {
      return loader.loadClass(name);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public Class<?> apply(String name) {
    return load(name);
  }
}


