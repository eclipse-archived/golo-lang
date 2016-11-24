/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;

import static java.util.Collections.unmodifiableSortedSet;
import static java.util.Collections.unmodifiableList;

class AugmentationDocumentation extends AbstractSet<FunctionDocumentation> implements SortedSet<FunctionDocumentation>, DocumentationElement {

  private String target;
  private String documentation;
  private int line;
  private DocumentationElement parent;
  private SortedSet<FunctionDocumentation> functions = new TreeSet<>();
  private List<String> augmentationNames = new LinkedList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return "augmentation";
  }

  public String target() {
    return target;
  }

  public AugmentationDocumentation target(String target) {
    this.target = target;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return target();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String id() {
    return "augment." + name();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return parent;
  }

  public AugmentationDocumentation parent(DocumentationElement p) {
    parent = p;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String documentation() {
    return (documentation != null) ? documentation : "\n";
  }

  public AugmentationDocumentation documentation(String documentation) {
    if (documentation != null) {
      this.documentation = documentation;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return line;
  }

  public AugmentationDocumentation line(int line) {
    this.line = line;
    return this;
  }

  public SortedSet<FunctionDocumentation> functions() {
    return unmodifiableSortedSet(functions);
  }

  public AugmentationDocumentation functions(Collection<FunctionDocumentation> docs) {
    if (docs != null) {
      this.functions.addAll(docs);
    }
    return this;
  }

  public List<String> augmentationNames() {
    return unmodifiableList(augmentationNames);
  }

  public AugmentationDocumentation augmentationNames(Collection<String> names) {
    if (names != null) {
      this.augmentationNames.addAll(names);
    }
    return this;
  }

  @Override
  public boolean add(FunctionDocumentation func) {
    return this.functions.add(func);
  }

  @Override
  public int size() { return functions.size(); }

  @Override
  public Iterator<FunctionDocumentation> iterator() { return functions.iterator(); }

  @Override
  public FunctionDocumentation last() { return functions.last(); }

  @Override
  public FunctionDocumentation first() { return functions.first(); }

  @Override
  public SortedSet<FunctionDocumentation> headSet(FunctionDocumentation e) {
    return functions.headSet(e);
  }

  @Override
  public SortedSet<FunctionDocumentation> tailSet(FunctionDocumentation e) {
    return functions.tailSet(e);
  }

  @Override
  public SortedSet<FunctionDocumentation> subSet(FunctionDocumentation f, FunctionDocumentation t) {
    return functions.subSet(f, t);
  }

  @Override
  public Comparator<? super FunctionDocumentation> comparator() {
    return functions.comparator();
  }
}
