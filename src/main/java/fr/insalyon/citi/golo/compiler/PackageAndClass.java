/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler;

import static fr.insalyon.citi.golo.compiler.utils.NamingUtils.extractTargetJavaClass;
import static fr.insalyon.citi.golo.compiler.utils.NamingUtils.extractTargetJavaPackage;

/**
 * Represents a package and class.
 */
public final class PackageAndClass {

  private final String packageName;
  private final String className;

  /**
   * Makes a new package and class definition.
   *
   * @param packageName the package name.
   * @param className   the class name.
   */
  public PackageAndClass(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
  }

  /**
   * Extracts a package and class definition from a string.
   *
   * @param qualifiedName a qualified name.
   * @return a package and class definition.
   */
  public static PackageAndClass fromString(String qualifiedName) {
    return new PackageAndClass(extractTargetJavaPackage(qualifiedName), extractTargetJavaClass(qualifiedName));
  }

  /**
   * @return a new {@code PackageAndClass} identifying an inner class of this class
   */
  public PackageAndClass createInnerClass(String name) {
    return new PackageAndClass(this.packageName, this.className + "$" + name);
  }

  /**
   * @return the package name.
   */
  public String packageName() {
    return packageName;
  }

  /**
   * @return the class name.
   */
  public String className() {
    return className;
  }

  @Override
  public String toString() {
    if (packageName.isEmpty()) {
      return className;
    } else {
      return packageName + "." + className;
    }
  }

  /**
   * @return a JVM type representation for this object, e.g.: {@code foo.Bar} gives {@code foo/Bar}.
   */
  public String toJVMType() {
    return toString().replaceAll("\\.", "/");
  }

  /**
   * @return a JVM reference type representation for this object, e.g.: {@code foo.Bar} gives
   * {@code Lfoo/Bar;}
   */
  public String toJVMRef() {
    return "L" + toJVMType() + ";";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PackageAndClass that = (PackageAndClass) o;

    if (className != null ? !className.equals(that.className) : that.className != null) return false;
    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31 * result + (className != null ? className.hashCode() : 0);
    return result;
  }
}
