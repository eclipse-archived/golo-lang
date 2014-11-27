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

package fr.insalyon.citi.golo.compiler.utils;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

public class OrderedRegister<K,V> extends LinkedHashMap<K,Set<V>> implements Register<K,V> {
  private static final long serialVersionUID = 1L;

  protected Set<V> emptyValue() {
    return new HashSet<>();
  }

  private Set<V> getOrInit(K key) {
    Set<V> bag;
    if (!containsKey(key)) {
      bag = emptyValue();
      put(key, bag);
    } else {
      bag = get(key);
    }
    return bag;
  }

  @Override
  public void add(K key, V value) {
    getOrInit(key).add(value);
  }

  @Override
  public void addAll(K key, Collection<V> values) {
    getOrInit(key).addAll(values);
  }
}
