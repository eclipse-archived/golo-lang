/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

/**
 * A reference to a class.
 *
 * <p>Used to represent a literal class notation.
 */
public final class ClassReference {
  private final String name;

  private ClassReference(String name) {
    this.name = java.util.Objects.requireNonNull(name);
  }

  public static ClassReference of(Object o) {
    if (o instanceof ClassReference) {
      return (ClassReference) o;
    }
    if (o instanceof Class<?>) {
      return new ClassReference(((Class<?>) o).getCanonicalName());
    }
    return new ClassReference(o.toString());
  }

  public String getName() {
    return this.name;
  }

  public String toJVMType() {
    return this.name.replaceAll("\\.", "#");
  }

  @Override
  public String toString() {
    return "Class<" + name + ">";
  }
}
