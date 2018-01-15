/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class PrimitiveArrayIteratorTest {

  @Test
  public void verify() {
    Object[] array = {1, 2, 3};
    PrimitiveArrayIterator iterator = new PrimitiveArrayIterator(array);

    assertThat(iterator.hasNext(), is(true));
    assertThat((Integer) iterator.next(), is(1));

    assertThat(iterator.hasNext(), is(true));
    assertThat((Integer) iterator.next(), is(2));

    assertThat(iterator.hasNext(), is(true));
    assertThat((Integer) iterator.next(), is(3));

    assertThat(iterator.hasNext(), is(false));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void no_remove() {
    Object[] array = {1, 2, 3};
    PrimitiveArrayIterator iterator = new PrimitiveArrayIterator(array);
    iterator.remove();
  }

  @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
  public void outofbounds_exception() {
    Object[] array = {};
    PrimitiveArrayIterator iterator = new PrimitiveArrayIterator(array);
    iterator.next();
  }
}
