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

import java.util.List;

public interface Alternatives<T extends GoloElement> {
  Alternatives<T> when(Object cond);

  Alternatives<T> then(Object action);

  Alternatives<T> otherwise(Object action);

  List<WhenClause<T>> getClauses();

  T getOtherwise();
}
