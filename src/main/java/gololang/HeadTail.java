/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import java.util.Iterator;

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
 *   when ht: isEmpty() then 0
 *   otherwhise 1 + size(ht: tail())
 * }
 * </pre>
 * <p>
 * Note that the {@code size} method is not provided since this interface
 * can be implemented by infinite generators.
 * <p>
 * A {@code List} augmentation is provided, as well as corresponding special
 * methods on arrays ({@code Object[]}/{@code array[]})
 *
 * @param <E> the type of the elements held in this structure.
 */
public interface HeadTail<E> extends Iterable<E> {

  /**
   * Get the head of the structure.
   *
   * @return the first element of the structure, or {@code null} if the structure is empty.
   */
  E head();

  /**
   * Get the tail of the structure.
   * <p>
   * To be side effect free, this method should return a deep copy of the original structure or an
   * immutable view on it.
   *
   * @return a new {@code HeadTail} containing all the elements but the first, or a empty one
   * if no elements remains.
   */
  HeadTail<E> tail();

  /**
   * Checks if the structure is empty or not.
   *
   * @return {@code true} if the structure contains no element, {@code false} otherwise.
   */
  boolean isEmpty();

  /**
   * Util method to wrap a {@code HeadTail} instance into an {@Iterable}
   *
   * @param headTail the instance to wrap
   * @return an iterable on the values contained in the wrapped instance
   */
  public static <E> Iterable<E> toIterable(HeadTail<E> headTail) {
    return new Iterable<E>() {
      @Override
      public Iterator<E> iterator() {
        return new HeadTailIterator<E>(headTail);
      }
    };
  }

}
