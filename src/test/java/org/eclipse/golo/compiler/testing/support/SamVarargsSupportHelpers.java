/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
