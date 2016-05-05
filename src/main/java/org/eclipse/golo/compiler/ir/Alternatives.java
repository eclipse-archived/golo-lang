/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
