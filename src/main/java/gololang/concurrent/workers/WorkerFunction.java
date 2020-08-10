/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.concurrent.workers;

/**
 * A worker function for asynchronously processing messages.
 * <p>
 * This interface is mostly used to facilitate the design of the Java API, as worker functions are made out of
 * function references in Golo.
 */
@FunctionalInterface
public interface WorkerFunction {

  /**
   * Called by a worker executor to process a message.
   *
   * @param message the message to process.
   */
  void apply(Object message);
}
