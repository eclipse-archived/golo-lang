package gololang.compiler.ast;

import gololang.compiler.utils.NamingUtils;

import static gololang.compiler.utils.NamingUtils.extractTargetJavaClass;
import static gololang.compiler.utils.NamingUtils.extractTargetJavaPackage;

public final class PackageAndClass {

  private final String packageName;
  private final String className;

  public PackageAndClass(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
  }

  public static PackageAndClass fromString(String qualifiedName) {
    return new PackageAndClass(extractTargetJavaPackage(qualifiedName), extractTargetJavaClass(qualifiedName));
  }

  public String packageName() {
    return packageName;
  }

  public String className() {
    return className;
  }

  @Override
  public String toString() {
    return packageName + "." + className;
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
