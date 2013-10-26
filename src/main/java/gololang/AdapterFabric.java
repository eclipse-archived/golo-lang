/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package gololang;

import fr.insalyon.citi.golo.runtime.adapters.AdapterDefinition;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class AdapterFabric {

  public static final class Maker {

    private final AdapterDefinition adapterDefinition;
    private final Class<?> adapterClass;

    private Maker(AdapterDefinition adapterDefinition, Class<?> adapterClass) {
      this.adapterDefinition = adapterDefinition;
      this.adapterClass = adapterClass;
    }

    public Object newInstance(Object... args) {
      throw new UnsupportedOperationException("Not yet, come back later!");
    }
  }

  private final ClassLoader classLoader;
  private final AtomicLong nextId = new AtomicLong();

  public AdapterFabric(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public AdapterFabric() {
    this(new ClassLoader(Thread.currentThread().getContextClassLoader()) {
    });
  }

  public static AdapterFabric withParentClassLoader(ClassLoader parentClassLoader) {
    return new AdapterFabric(new ClassLoader(parentClassLoader) {
    });
  }

  public Maker maker(Map<String, Object> configuration) {
    throw new UnsupportedOperationException("Not yet, come back later!");
  }
}
