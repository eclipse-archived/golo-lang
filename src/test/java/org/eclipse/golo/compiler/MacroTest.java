/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import org.testng.annotations.Test;

import org.eclipse.golo.internal.testing.GoloTest;

public class MacroTest extends GoloTest {

  @Override
  public String srcDir() {
    return "for-macros/";
  }

  private void loadAndRun(String moduleName) throws Throwable {
    load(moduleName + "-macros");
    run(moduleName);
  }

  @Test
  public void asExpression() throws Throwable {
    loadAndRun("asExpression");
  }

  @Test
  public void decoratorLike() throws Throwable {
    loadAndRun("decoratorlike");
  }

  @Test
  public void recur() throws Throwable {
    loadAndRun("recur");
  }

  @Test
  public void sideeffect() throws Throwable {
    loadAndRun("sideeffect");
  }

  @Test
  public void simple() throws Throwable {
    loadAndRun("simple");
  }

  @Test
  public void toplevel() throws Throwable {
    loadAndRun("toplevel");
  }

  @Test
  public void usingFunctions() throws Throwable {
    loadAndRun("usingFunctions");
  }

  @Test
  public void varargs() throws Throwable {
    loadAndRun("varargs");
  }

  @Test
  public void quote() throws Throwable {
    loadAndRun("test-quote");
  }

  @Test
  public void resolveFQN() throws Throwable {
    load("asExpression-macros");
    loadAndRun("resolve-fqn");
  }

  @Test(expectedExceptions=org.eclipse.golo.compiler.GoloCompilationException.class)
  public void resolveFQNFails() throws Throwable {
    load("asExpression-macros");
    load("resolve-fqn-macros");
    run("resolve-fqn-fails");
  }

  @Test
  public void withInit() throws Throwable {
    load("with-init-macros");
    load("with-init-macros2");
    run("with-init");
  }
}
