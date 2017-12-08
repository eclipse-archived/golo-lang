/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime.augmentation;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Encapsulate runtime information about augmentation method resolution.
 */
public final class AugmentationMethod implements Comparable<AugmentationMethod> {

  private final AugmentationApplication.Kind kind;
  private final DefiningModule.Scope scope;
  private final Class<?> target;
  private final Method method;

  AugmentationMethod(AugmentationApplication.Kind kind, DefiningModule.Scope scope, Class<?> target, Method method) {
    this.kind = kind;
    this.scope = scope;
    this.target = target;
    this.method = method;
  }

  public Method method() {
    return method;
  }

  @Override
  public String toString() {
    return String.format("AugmentationMethod<%s,%s,%s,%s>",
        kind, scope, target, method);
  }

  /**
   * Compare applications for priority.
   * <p>
   * The greater application is the one with the lower priority so that in a stable-sorted list of
   * application, we always take the first one.
   * <p>
   * The natural order defined is:
   * {@code target specificity < scope < kind}
   * so that the implied priority is to use the augmentation defined:
   * <ol>
   * <li> on the more specific class: to allow to override augmentations by target specificity,
   * similar to method overriding;
   * <li> in the more local scope: to allow to override augmentations by defining them locally;
   * <li> by direct definition instead of named application: to allow to augment a class with a
   * named augmentation (mixin-like) but override some of the methods by directly defining them;
   * <li> before any other augmentation: to control augmentation application using the import or
   * definition order.
   * </ol>
   * Moreover, augmentation method with fixed arity are used in preference to variable-arity ones.
   */
  @Override
  public int compareTo(AugmentationMethod other) {
    if (this.target.isAssignableFrom(other.target) && !this.target.equals(other.target)) {
      return 1;
    }
    if (other.target.isAssignableFrom(this.target) && !this.target.equals(other.target)) {
      return -1;
    }
    if (this.scope != other.scope) {
      return this.scope.compareTo(other.scope);
    }
    if (this.kind != other.kind) {
      return this.kind.compareTo(other.kind);
    }
    if (this.method.isVarArgs() && !other.method.isVarArgs()) {
      return 1;
    }
    if (other.method.isVarArgs() && !this.method.isVarArgs()) {
      return -1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) { return false; }
    if (this == o) { return true; }
    if (!this.getClass().equals(o.getClass())) { return false; }
    AugmentationMethod that = (AugmentationMethod) o;
    return this.kind == that.kind
      && this.scope == that.scope
      && this.target.equals(that.target)
      && this.method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, scope, target, method);
  }
}

