/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang.concurrent.workers;

/**
 * A worker function for asynchronously processing messages.
 * <p>
 * This interface is mostly used to facilitate the design of the Java API, as worker functions are made out of
 * function references in Golo.
 */
public interface WorkerFunction {

  /**
   * Called by a worker executor to process a message.
   *
   * @param message the message to process.
   */
  void apply(Object message);
}
