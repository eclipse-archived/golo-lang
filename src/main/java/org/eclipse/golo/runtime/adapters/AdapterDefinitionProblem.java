/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime.adapters;

public class AdapterDefinitionProblem extends RuntimeException {

  public AdapterDefinitionProblem(String message) {
    super(message);
  }

  public AdapterDefinitionProblem(Throwable cause) {
    super(cause);
  }

  public AdapterDefinitionProblem(String message, Throwable cause) {
    super(message, cause);
  }
}
