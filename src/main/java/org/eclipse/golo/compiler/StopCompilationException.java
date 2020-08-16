/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

/**
 * Exception to stop the compilation process.
 *
 * <p>No stacktrace is available to make it lightweight.
 * Usefull in macros to stop the compilation without all the usual error stuffs.
 */
public class StopCompilationException extends RuntimeException {
  public StopCompilationException() {
    this(null, null);
  }

  public StopCompilationException(String message) {
    this(message, null);
  }

  public StopCompilationException(Throwable cause) {
    this(null, cause);
  }

  public StopCompilationException(String message, Throwable cause) {
    super(message, cause, true, false);
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
