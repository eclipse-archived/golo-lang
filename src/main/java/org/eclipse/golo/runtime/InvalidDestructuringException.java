/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

public class InvalidDestructuringException extends IllegalArgumentException {
  // TODO: localize the error message?

  public InvalidDestructuringException(String message) {
    super(message);
  }

  public InvalidDestructuringException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidDestructuringException(Throwable cause) {
    super(cause);
  }

  public static InvalidDestructuringException tooManyValues(int expected) {
    return new InvalidDestructuringException(String.format(
          "too many values (expecting %d).",
            expected));
  }

  public static InvalidDestructuringException notEnoughValues(int expected, boolean sub) {
    return new InvalidDestructuringException(String.format(
          "not enough values (expecting%s %d).",
            sub ? " at least" : "",
            sub ? expected - 1 : expected));
  }

  public static InvalidDestructuringException notEnoughValues(int expected, int available, boolean sub) {
    return new InvalidDestructuringException(String.format(
          "not enough values (expecting%s %d and got %d).",
            sub ? " at least" : "",
            sub ? expected - 1 : expected,
            available));
  }

}
