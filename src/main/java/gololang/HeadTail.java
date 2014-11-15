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

package gololang;

/**
 * Structure having a head and a tail.
 * <p>
 * This interface can be used to define any recursive sequence of object,
 * such as lists or generators. It is well suited to recursive algorithms, and
 * thus can be seen as an alternative to the {@code Iterable} interface.
 * <p>
 * For example, one can compute the size of such an object with:
 * <pre>
 * static int size(HeadTail<?> o) {
 *   if (o.isEmpty()) { return 0; }
 *   return 1 + size(o.tail());
 * }
 * </pre>
 * or in Golo:
 * <pre>
 * function size = |ht| -> match {
 *   when ht: isEmpty then 0
 *   otherwhise 1 + size(ht: tail())
 * }
 *
 * A {@code List} augmentation is provided, as well as corresponding special
 * methods on arrays ({@code Object[]}/{@code array[]})
 */
public interface HeadTail<T> extends Iterable<T> {
  /**
   * Returns the first element of the collection.
   */
  T head();

  /**
   * Returns a new {@code HeadTail} containing the rest of the elements.
   */
  HeadTail<T> tail();

  /**
   * Checks if the structure is empty.
   */
  boolean isEmpty();
}
