/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
