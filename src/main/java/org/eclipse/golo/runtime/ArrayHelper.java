/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import java.util.NoSuchElementException;
import java.util.Objects;
import gololang.Tuple;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.copyOf;

public final class ArrayHelper {

  private ArrayHelper() {
    throw new UnsupportedOperationException("Don't instantiate utility classes");
  }

  public static Object head(Object[] array) {
    if (array.length == 0) {
      return null;
    }
    return array[0];
  }

  public static Object[] tail(Object[] array) {
    if (array.length >= 1) {
      return copyOfRange(array, 1, array.length);
    }
    return new Object[0];
  }

  public static Object first(Object[] array) {
    if (array.length == 0) {
      throw new NoSuchElementException("Empty array");
    }
    return array[0];
  }

  public static Object last(Object[] array) {
    if (array.length == 0) {
      throw new NoSuchElementException("Empty array");
    }
    return array[array.length - 1];
  }

  public static boolean isEmpty(Object[] array) {
    return array.length == 0;
  }

  public static boolean contains(Object[] array, Object elt) {
    for (Object o : array) {
      if (Objects.deepEquals(o, elt)) {
        return true;
      }
    }
    return false;
  }

  public static int indexOf(Object[] array, Object elt) {
    for (int i = 0; i < array.length; i++) {
      if (Objects.deepEquals(array[i], elt)) {
        return i;
      }
    }
    return -1;
  }

  public static Object[] nullify(Object[] array, Object[] toSkip) {
    if (array.length != toSkip.length) {
      throw new IllegalArgumentException("Both array must have the same length");
    }
    for (int i = 0; i < array.length; i++) {
      if (Boolean.valueOf(true).equals(toSkip[i])) { // cast + check for null all in one
        array[i] = null;
      }
    }
    return array;
  }

  public static Object[] newStyleDestruct(Object[] array, int number, boolean substruct, Object[] toSkip) {
    if (number < array.length && !substruct) {
      throw InvalidDestructuringException.notEnoughValues(number, array.length, substruct);
    }
    if (number == array.length && !substruct) {
      return nullify(copyOf(array, number), toSkip);
    }
    if (number <= array.length && substruct) {
      Object[] destruct = new Object[number];
      System.arraycopy(array, 0, destruct, 0, number - 1);
      if (Boolean.valueOf(false).equals(toSkip[number - 1])) {
        destruct[number - 1] = copyOfRange(array, number - 1, array.length);
      }
      return nullify(destruct, toSkip);
    }
    if (number == array.length + 1 && substruct) {
      Object[] destruct = copyOf(array, number);
      if (Boolean.valueOf(false).equals(toSkip[number - 1])) {
        destruct[number - 1] = new Object[0];
      }
      return nullify(destruct, toSkip);
    }
    throw InvalidDestructuringException.tooManyValues(number);
  }
}
