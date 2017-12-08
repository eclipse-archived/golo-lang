/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;

public class AugmentationResolutionTest extends GoloTest {
  @Override
  protected String srcDir() {
    return "for-execution/call-resolution/";
  }

  @Test
  public void callStackLookup() throws Throwable {
    load("data");
    load("lib");
    run("call-stack-lookup");
  }

  @Test
  public void localNamedAugmentations() throws Throwable {
    run("local-named-augmentations");
  }

  @Test
  public void externalNamedAugmentations() throws Throwable {
    load("named-augmentations-external-source");
    run("external-named-augmentations");
  }

  @Test
  public void mixinComplexDispatch() throws Throwable {
    load("mixin-augmentation");
    load("mixin-data");
    load("mixin-lib");
    run("mixin-test");
  }
}
