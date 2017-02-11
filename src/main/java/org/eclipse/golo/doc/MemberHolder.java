/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.doc;

import java.util.List;
import java.util.Collection;

interface MemberHolder {
  MemberDocumentation addMember(String name);

  List<MemberDocumentation> members();

  MemberHolder members(Collection<MemberDocumentation> m);

  default boolean hasMembers() {
    return !members().isEmpty();
  }
}
