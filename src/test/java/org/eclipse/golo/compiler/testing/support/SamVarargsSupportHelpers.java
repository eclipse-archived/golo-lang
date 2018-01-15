/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.testing.support;

public class SamVarargsSupportHelpers {

  public static interface EchoVA {
    public Object echo(Object... echo);
  }

  public static Object apply(EchoVA echo) {
    return echo.echo("Hey!","Hey!");
  }

  public Object state;

  public SamVarargsSupportHelpers(EchoVA echo) {
    state = echo.echo(new Object[]{"Plop","Plop"});
  }

  public Object plopIt(EchoVA echo, String what) {
    return echo.echo(what,what);
  }
}
