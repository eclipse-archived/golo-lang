/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import static java.util.Objects.requireNonNull;

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
    this.packageName = requireNonNull(packageName);
    this.className = requireNonNull(className);
    if (className.isEmpty()) {
      throw new IllegalArgumentException("The class name can't be empty");
    }
  }

  /**
   * Extracts a package and class definition from a string.
   *
   * @param qualifiedName a qualified name.
   * @return a package and class definition.
   */
  public static PackageAndClass fromString(String qualifiedName) {
    return new PackageAndClass(
        extractTargetJavaPackage(qualifiedName),
        extractTargetJavaClass(qualifiedName));
  }

  private static int packageClassSeparatorIndex(String moduleName) {
    if (moduleName != null) {
      return moduleName.lastIndexOf('.');
    }
    return -1;
  }

  private static String extractTargetJavaPackage(String moduleName) {
    int packageClassSeparatorIndex = packageClassSeparatorIndex(moduleName);
    if (packageClassSeparatorIndex > 0) {
      return moduleName.substring(0, packageClassSeparatorIndex);
    }
    return "";
  }

  private static String extractTargetJavaClass(String moduleName) {
    int packageClassSeparatorIndex = packageClassSeparatorIndex(moduleName);
    if (packageClassSeparatorIndex > 0) {
      return moduleName.substring(packageClassSeparatorIndex + 1);
    }
    return moduleName;
  }


  /**
   * Create an inner class.
   * <p>
   * For instance:
   * <code><pre>
   * PackageAndClass cls = new PackageAndClass("foo.bar", "Spam");
   * PackageAndClass inner = cls.createInnerClass("Egg"); // foo.bar.Spam$Egg
   * </pre></code>
   *
   * @return a new {@code PackageAndClass} identifying an inner class of this class.
   */
  public PackageAndClass createInnerClass(String name) {
    return new PackageAndClass(
        this.packageName,
        this.className + "$" + name.replace('.', '$'));
  }

  /**
   * Create a sibling class.
   * <p>
   * For instance:
   * <code><pre>
   *  PackageAndClass list = new PackageAndClass("java.util", "List");
   *  PackageAndClass set = list.createSiblingClass("Set"); // java.util.Set
   * </pre></code>
   *
   * @return a new {@code PackageAndClass} identifying an alternate class in the same package.
   */
  public PackageAndClass createSiblingClass(String name) {
    return new PackageAndClass(this.packageName, name);
  }

  /**
   * Create a sub-package.
   * <p>
   * For instance:
   * <code><pre>
   *  PackageAndClass pc = new PackageAndClass("foo.bar", "Module");
   *  PackageAndClass sub = pc.createSubPackage("types"); // foo.bar.Modules.types
   * </pre></code>
   *
   * @return a new {@code PackageAndClass} identifying a sub-package of this package.
   */
  public PackageAndClass createSubPackage(String name) {
    return new PackageAndClass(
        this.packageName.isEmpty() ? this.className : this.packageName + "." + this.className, name);
  }

  /**
   * Create a class in another package.
   * <p>
   * For instance:
   * <code><pre>
   * PackageAndClass pc = PackageAndClass.fromString("foo.bar.Baz");
   * PackageAndClass other = pc.inPackage("plic.ploc"); // plic.ploc.Baz
   * </pre></code>
   *
   * @return a new {@code PackageAndClass} representing the same class in another package.
   * @param qualifiedName the qualified name of the new package.
   */
  public PackageAndClass inPackage(String qualifiedName) {
    return new PackageAndClass(qualifiedName, className);
  }

  /**
   * Create a class in another the same package as another one.
   * <p>
   * For instance:
   * <code><pre>
   * PackageAndClass pc = PackageAndClass.fromString("foo.bar.Baz");
   * PackageAndClass other = PackageAndClass.fromString("plic.ploc.Foo");
   * PackageAndClass newOne = pc.inPackage(other); // plic.ploc.Baz
   * </pre></code>
   *
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
