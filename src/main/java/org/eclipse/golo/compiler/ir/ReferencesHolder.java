/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

/**
 * Interface for elements that contains local references.
 */
interface ReferencesHolder {

  /**
   * Returns the references holden by this element.
   */
  LocalReference[] getReferences();

  /**
   * Returns the number of references contained in this element.
   */
  default int getReferencesCount() {
    return getReferences().length;
  }

  /**
   * Returns only the declaring references.
   */
  default LocalReference[] getDeclaringReferences() {
    return getReferences();
  }
}
