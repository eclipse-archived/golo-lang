/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.compiler.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

public abstract class AbstractRegister<K, V> extends AbstractMap<K,Set<V>> implements Register<K,V> {

  private Map<K, Set<V>> map;

  public AbstractRegister() {
    super();
    this.map = initMap();
  };

  abstract protected Map<K, Set<V>> initMap();

  abstract protected Set<V> emptyValue();

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
  public Set<Map.Entry<K,Set<V>>> entrySet() {
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
