/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.util.function.Function;

/**
 * A {@code Loader} just encapsulate a {@code ClassLoader}. The class implements
 * {@code java.util.function.Function<String, Class<?>>} so that a loader can be directly mapped to a stream of the
 * names of the classes to load. Moreover, the {@code ClassNotFoundException} is catched to return
 * {@code null}, allowing a more "flowing" use. For instance:
 * <pre><code>
 * Loader loader = new Loader(this.getClass().getClassLoader());
 * Stream.of("java.lang.String", "gololang.Tuple", "foo.bar.Baz", "java.util.LinkedList")
 *     .map(loader)
 *     .filter(java.util.Objects::nonNull)
 *     .map(klass -> dealWithTheClass(klass))
 * </code></pre>
 */
public final class Loader implements Function<String, Class<?>> {
  private final ClassLoader loader;

  Loader(ClassLoader loader) {
    this.loader = loader;
  }

  /**
   * Create a loader using the {@code ClassLoader} of the given class
   *
   * @param klass the class whose {@code ClassLoader} to use
   * @return a {@code Loader}
   * */
  public static Loader forClass(Class<?> klass) {
    return new Loader(klass.getClassLoader());
  }

  /**
   * Load the given class.
   * <p>
   * This only delegates to the underlying class loader, but returns {@code null} instead
   * of throwing a {@code ClassNotFoundException} exception.
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

  /**
   * Just delegate to {@link #load(java.lang.String)} to implement {@code Function}.
   *
   * @see #load(java.lang.String)
   */
  @Override
  public Class<?> apply(String name) {
    return load(name);
  }
}


