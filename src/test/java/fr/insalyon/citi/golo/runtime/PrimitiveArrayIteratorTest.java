/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.runtime;

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
