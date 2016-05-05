/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.utils.NamingUtils;

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
    return new PackageAndClass(NamingUtils.extractTargetJavaPackage(qualifiedName), NamingUtils.extractTargetJavaClass(qualifiedName));
  }

  /**
   * @return a new {@code PackageAndClass} identifying an inner class of this class.
   */
  public PackageAndClass createInnerClass(String name) {
    return new PackageAndClass(
        this.packageName,
        this.className + "$" + name.replace('.', '$'));
  }

  /**
   * @return a new {@code PackageAndClass} representing the same class in another package.
   * @param qualifiedName the qualified name of the new package.
   */
  public PackageAndClass inPackage(String qualifiedName) {
    return new PackageAndClass(qualifiedName, className);
  }

  /**
   * @return a new {@code PackageAndClass} representing the same class in another package.
   * @param parent the {@code PackageAndClass} representing the new package.
   */
  public PackageAndClass inPackage(PackageAndClass parent) {
    return new PackageAndClass(parent.packageName(), className);
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

  /**
   * @return a mangled named for this class.
   */
  public String mangledName() {
    return toString().replace('.', '$');
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
