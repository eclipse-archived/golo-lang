/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

rootProject.name = "golo"

if (!JavaVersion.current().isJava8) {
  throw GradleException("Golo is not fully compatible with Java 9 and beyond, please use Java 8 to build it")
}

