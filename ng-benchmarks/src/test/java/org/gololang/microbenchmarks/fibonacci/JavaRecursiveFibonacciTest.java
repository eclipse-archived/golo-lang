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

package org.gololang.microbenchmarks.fibonacci;

import org.junit.Test;

import static org.gololang.microbenchmarks.fibonacci.JavaRecursiveFibonacci.withBoxing;
import static org.gololang.microbenchmarks.fibonacci.JavaRecursiveFibonacci.withPrimitives;
import static org.junit.Assert.assertEquals;

public class JavaRecursiveFibonacciTest {

  @Test
  public void test_withPrimitives() throws Exception {
    assertEquals(1L, withPrimitives(1L));
    assertEquals(1L, withPrimitives(2L));
    assertEquals(2L, withPrimitives(3L));
    assertEquals(3L, withPrimitives(4L));
    assertEquals(5L, withPrimitives(5L));
    assertEquals(8L, withPrimitives(6L));
    assertEquals(13L, withPrimitives(7L));
  }

  @Test
  public void test_withBoxing() throws Exception {
    assertEquals((Long) 1L, withBoxing(1L));
    assertEquals((Long) 1L, withBoxing(2L));
    assertEquals((Long) 2L, withBoxing(3L));
    assertEquals((Long) 3L, withBoxing(4L));
    assertEquals((Long) 5L, withBoxing(5L));
    assertEquals((Long) 8L, withBoxing(6L));
    assertEquals((Long) 13L, withBoxing(7L));
  }
}
