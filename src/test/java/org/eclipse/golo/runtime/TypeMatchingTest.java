/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import static org.eclipse.golo.runtime.TypeMatching.compareSubstituable;
import static org.eclipse.golo.runtime.TypeMatching.compareTypes;

public class TypeMatchingTest {
  @Test
  public void compareSubstituable_numbers() {
    assertThat(compareSubstituable(int.class, int.class) == 0, is(true));
    assertThat(compareSubstituable(int.class, float.class) < 0, is(true));
    assertThat(compareSubstituable(float.class, int.class) > 0, is(true));
    assertThat(compareSubstituable(int.class, Object.class) < 0, is(true));
    assertThat(compareSubstituable(int.class, Integer.class) == 0, is(true));
    assertThat(compareSubstituable(int.class, Float.class) < 0, is(true));
    assertThat(compareSubstituable(Integer.class, Float.class) < 0, is(true));
    assertThat(compareSubstituable(Integer.class, float.class) < 0, is(true));
  }

  @Test
  public void compareSubstituable_objects() {
    assertThat(compareSubstituable(java.util.ArrayList.class, java.util.ArrayList.class) == 0, is(true));
    assertThat(compareSubstituable(java.util.ArrayList.class, java.util.List.class) < 0, is(true));
    assertThat(compareSubstituable(java.util.ArrayList.class, java.util.Collection.class) < 0, is(true));
    assertThat(compareSubstituable(java.util.List.class, java.util.Collection.class) < 0, is(true));
    assertThat(compareSubstituable(java.util.Collection.class, java.util.ArrayList.class) > 0, is(true));
    assertThat(compareSubstituable(java.util.List.class, Object.class) < 0, is(true));
  }

  @Test
  public void compareSubstituable_not_comparable() {
    assertThat(compareSubstituable(java.util.List.class, int.class) == 0, is(true));
    assertThat(compareSubstituable(int.class, java.util.List.class) == 0, is(true));
    assertThat(compareSubstituable(String.class, java.util.List.class) == 0, is(true));
  }

  @Test
  public void compareTypes_numbers() {
    assertThat(compareTypes(ta(int.class, float.class), ta(int.class, float.class)) == 0, is(true));
    assertThat(compareTypes(ta(int.class, int.class), ta(int.class, float.class)) < 0, is(true));
    assertThat(compareTypes(ta(int.class, int.class), ta(float.class, float.class)) < 0, is(true));
  }

  private static Class<?>[] ta(Class<?>... c) {
    return c;
  }
}
