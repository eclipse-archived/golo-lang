/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.doc;

import java.util.*;
import gololang.FunctionReference;

/**
 * Store the (public) elements defined in order to generate a global index.
 */
class DocIndex implements Iterable<DocumentationElement> {

  private SortedSet<DocumentationElement> elements = new TreeSet<>();

  public void update(ModuleDocumentation moduleDoc) {
    for (DocumentationElement e : moduleDoc.functions()) {
      elements.add(e);
    }
    for (StructDocumentation e : moduleDoc.structs()) {
      elements.add(e);
      for (MemberDocumentation m : e.members()) {
        elements.add(m);
      }
    }
    for (UnionDocumentation e : moduleDoc.unions()) {
      elements.add(e);
      for (UnionDocumentation.UnionValueDocumentation v : e.values()) {
        elements.add(v);
        for (MemberDocumentation m : v.members()) {
          elements.add(m);
        }
      }
    }
    for (NamedAugmentationDocumentation e : moduleDoc.namedAugmentations()) {
      elements.add(e);
      for (DocumentationElement f : e.functions()) {
        elements.add(f);
      }
    }
    for (AugmentationDocumentation e : moduleDoc.augmentations()) {
      elements.add(e);
      for (DocumentationElement f : e.functions()) {
        elements.add(f);
      }
    }
  }

  public Map<String, Set<DocumentationElement>> groupBy(FunctionReference f) throws Throwable {
    TreeMap<String, Set<DocumentationElement>> map = new TreeMap<>();
    for (DocumentationElement e : elements) {
      String k = f.invoke(e).toString();
      if (!map.containsKey(k)) {
        map.put(k, new TreeSet<DocumentationElement>());
      }
      map.get(k).add(e);
    }
    return map;
  }

  @Override
  public Iterator<DocumentationElement> iterator() {
    return elements.iterator();
  }


}
