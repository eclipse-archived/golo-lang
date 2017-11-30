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

import java.util.stream.Stream;
import org.eclipse.golo.runtime.MethodInvocation;

import static java.lang.reflect.Modifier.*;
import static org.eclipse.golo.runtime.augmentation.DefiningModule.Scope;

/**
 * Encapsulate runtime information for an augmentation application resolution.
 */
public final class AugmentationApplication {

  enum Kind { SIMPLE, NAMED }

  private final Class<?> augmentation;
  private final Class<?> target;
  private final Scope scope;
  private final Kind kind;

  AugmentationApplication(Class<?> augmentation, Class<?> target, Scope scope, Kind kind) {
    this.scope = scope;
    this.target = target;
    this.augmentation = augmentation;
    this.kind = kind;
  }

  @Override
  public String toString() {
    return String.format("AugmentationApplication<%s,%s,%s,%s>", augmentation, target, scope, kind);
  }

  public Stream<AugmentationMethod> methodsMaching(MethodInvocation invocation) {
    if (augmentation == null) {
      return Stream.empty();
    }
    return Stream.of(augmentation.getMethods())
      .filter(method -> invocation.match(method))
      .map(method -> new AugmentationMethod(kind, scope, target, method));
  }
}

