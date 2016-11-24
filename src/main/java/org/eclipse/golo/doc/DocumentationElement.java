/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

interface DocumentationElement extends Comparable<DocumentationElement> {

  /**
   * The simple name of the element.
   */
  String name();

  /**
   * The documentation comment for the element.
   */
  String documentation();

  /**
   * The line where the element is defined.
   */
  int line();

  /**
   * The parent element.
   * <p>
   * For toplevel element, this is the module. For functions, it can be the augmentation. For union values, it is the
   * union.
   */
  DocumentationElement parent();

  /**
   * A readable name for the kind of element.
   */
  String type();

  /**
   * The fully qualified named of the element.
   * <p>
   * Typically, the parent full name and the element name.
   */
  default String fullName() {
    return parent().fullName() + "." + name();
  }

  /**
   * A unique identifier for the element.
   * <p>
   * Can be used in html ID for instance.
   */
  default String id() {
    if (parent() != null && !parent().id().isEmpty()) {
      return parent().id() + '.' + name();
    }
    return name();
  }

  /**
   * A readable representation of the element.
   * <p>
   * Typically the name, but can also contains argument names for functions for instance.
   */
  default String label() {
    return name();
  }

  default int compareTo(DocumentationElement other) {
    if (this == other) { return 0; }
    if (other == null) { return 1; }
    int c = label().compareToIgnoreCase(other.label());
    if (c == 0) {
      c = type().compareTo(other.type());
    }
    if (c == 0 && parent() != this) {
      c = parent().compareTo(other.parent());
    }
    return c;
  }
}

