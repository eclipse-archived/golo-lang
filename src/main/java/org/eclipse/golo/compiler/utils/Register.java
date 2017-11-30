/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.utils;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

public interface Register<K, V> extends Map<K, Set<V>> {
  void add(K key, V value);
  void addAll(K key, Collection<V> values);
  void updateKey(K oldKey, K newKey);
}

