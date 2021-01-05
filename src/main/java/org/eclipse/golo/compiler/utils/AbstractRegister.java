/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

public abstract class AbstractRegister<K, V> extends AbstractMap<K, Set<V>> implements Register<K, V> {

  private final Map<K, Set<V>> map;

  public AbstractRegister() {
    super();
    this.map = initMap();
  }

  protected abstract Map<K, Set<V>> initMap();

  protected abstract Set<V> emptyValue();

  @SuppressWarnings("unchecked")
  private Set<V> getOrInit(Object key) {
    Set<V> bag;
    if (!this.map.containsKey(key)) {
      bag = emptyValue();
      this.map.put((K) key, bag);
    } else {
      bag = this.map.get(key);
    }
    return bag;
  }

  @Override
  public Set<Map.Entry<K, Set<V>>> entrySet() {
    return map.entrySet();
  }

  @Override
  public Set<V> put(K key, Set<V> value) {
    return this.map.put(key, value);
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public Set<V> get(Object key) {
    return getOrInit(key);
  }

  @Override
  public void add(K key, V value) {
    getOrInit(key).add(value);
  }

  @Override
  public void addAll(K key, Collection<V> values) {
    getOrInit(key).addAll(values);
  }

  @Override
  public void updateKey(K oldKey, K newKey) {
    if (this.map.containsKey(oldKey)) {
      this.map.put(newKey, this.map.get(oldKey));
      this.map.remove(oldKey);
    }
  }
}
