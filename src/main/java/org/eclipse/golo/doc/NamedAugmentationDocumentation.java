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
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;

class NamedAugmentationDocumentation extends AbstractSet<FunctionDocumentation> implements DocumentationElement {

  private String name;
  private String documentation;
  private int line;
  private DocumentationElement parent;
  private Set<FunctionDocumentation> functions = new TreeSet<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return "named augmentation";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String label() {
    return name;
  }

  public NamedAugmentationDocumentation name(String name) {
    this.name = name;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return parent;
  }

  public NamedAugmentationDocumentation parent(DocumentationElement p) {
    parent = p;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String documentation() {
    return (documentation != null ? documentation : "");
  }

  public NamedAugmentationDocumentation documentation(String documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return line;
  }

  public NamedAugmentationDocumentation line(int line) {
    this.line = line;
    return this;
  }

  public Set<FunctionDocumentation> functions() {
    return unmodifiableSet(functions);
  }

  public NamedAugmentationDocumentation functions(Collection<FunctionDocumentation> docs) {
    this.functions.addAll(docs);
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

}

