/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.testing.support;

public class SamSupportHelpers {

  public interface Echo {
    Object echo(Object echo);
  }

  public static Object apply(Echo echo) {
    return echo.echo("Hey!");
  }

  public Object state;

  public SamSupportHelpers(Echo echo) {
    state = echo.echo("Plop");
  }

  public Object plopIt(Echo echo, String what) {
    return echo.echo(what);
  }
}
