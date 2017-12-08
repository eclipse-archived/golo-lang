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
import java.util.function.Predicate;
import org.eclipse.golo.runtime.Loader;
import org.eclipse.golo.runtime.Module;

import static org.eclipse.golo.runtime.augmentation.AugmentationApplication.Kind;

/**
 * Encapsulate a module defining an augmentation.
 */
public final class DefiningModule {

  public enum Scope { LOCAL, IMPORT, CALLSTACK }

  private final Class<?> module;
  private final Scope scope;

  DefiningModule(Class<?> module, Scope scope) {
    this.module = module;
    this.scope = scope;
  }

  public Class<?> module() {
    return this.module;
  }

  public static DefiningModule of(Class<?> module, Scope scope) {
    return new DefiningModule(module, scope);
  }

  public static DefiningModule ofLocal(Class<?> module) {
    return new DefiningModule(module, Scope.LOCAL);
  }

  public static DefiningModule ofImport(Class<?> module) {
    return new DefiningModule(module, Scope.IMPORT);
  }

  public static DefiningModule ofCallstack(Class<?> module) {
    return new DefiningModule(module, Scope.CALLSTACK);
  }

  /**
   * Returns a stream of augmentations definitions defined directly on a type in the corresponding
   * module.
   * e.g.
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * augment module.Type {
   *    # ...
   * }
   * </code></pre>
   */
  private Stream<AugmentationApplication> simpleAugmentationsFor(Loader loader, Class<?> receiverType) {
    return Stream.of(Module.augmentations(module))
      .map(loader)
      .filter(isAssignableFrom(receiverType))
      .map(target -> new AugmentationApplication(
            loader.load(module.getName() + "$" + target.getName().replace(".", "$")),
            target, scope, Kind.SIMPLE));
  }

  private static Predicate<Class<?>> isAssignableFrom(Class<?> receiver) {
    return target -> target != null && target.isAssignableFrom(receiver);
  }

  private Stream<AugmentationApplication> fullyNamedAugmentationsFor(Loader loader, Class<?> receiverType) {
    return Stream.of(Module.augmentationApplications(module))
      .map(targetName -> loader.load(targetName))
      .filter(target -> target != null && target.isAssignableFrom(receiverType))
      .flatMap(target -> qualifyAugmentations(loader, target));
  }

  private Stream<AugmentationApplication> qualifyAugmentations(Loader loader, Class<?> target) {
    return Stream.of(Module.augmentationApplications(module, target))
      .flatMap(augmentName -> fullyQualifiedName(augmentName))
      .map(augmentName -> new AugmentationApplication(
              loader.load(augmentName),
              target, scope, Kind.NAMED));
  }

  /**
   * Fully qualify an augmentation name.
   * <p>
   * Given an augmentation name, this generate a stream of alternative names by prepending
   * names of the defining module as well as imported modules.
   */
  private Stream<String> fullyQualifiedName(String augmentationName) {
    Stream.Builder<String> names = Stream.builder();
    int idx = augmentationName.lastIndexOf(".");
    if (idx == -1) {
      names.add(augmentationName);
    } else {
      names.add(new StringBuilder(augmentationName).replace(idx, idx + 1, "$").toString());
    }
    names.add(module.getName() + "$" + augmentationName.replace(".", "$"));
    return Stream.concat(
        names.build(),
        Stream.of(Module.imports(module))
        .map(prefix -> prefix + "$" + augmentationName.replace(".", "$")));
  }

  public Stream<AugmentationApplication> augmentationsFor(Loader loader, Class<?> receiverType) {
    if (module == null) {
      return Stream.empty();
    }
    return Stream.concat(
        simpleAugmentationsFor(loader, receiverType),
        fullyNamedAugmentationsFor(loader, receiverType));

  }

  @Override
  public String toString() {
    return "DefiningModule<" + module + "," + scope + ">";
  }
}


