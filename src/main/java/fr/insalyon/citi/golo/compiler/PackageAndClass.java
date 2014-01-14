/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
   * @return a JVM type representation for this object, e.g.: <code>foo.Bar</code> gives <code>foo/Bar</code>.
   */
  public String toJVMType() {
    return toString().replaceAll("\\.", "/");
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
