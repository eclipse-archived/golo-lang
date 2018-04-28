/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.concurrent.async;

/**
 * A future is an abstraction over the eventual result of a possibly asynchronous computation.
 *
 * This interface is intended to be used in conjunction with {@code Promise}. A future is a read-only view over a
 * promise.
 *
 * {@code Future} objects are made composable in Golo through a set of augmentations: {@code filter}, {@code map}, etc.
 * You should consult the "golodoc" of the {@code gololang.Async} module.
 */
public interface Future {

  /**
   * Non-blocking get.
   *
   * @return the future value, which may be {@code null} if it has not been resolved yet.
   */
  Object get();

  /**
   * Blocking get, waiting until the future has been resolved.
   *
   * @return the future value.
   * @throws InterruptedException when the current thread is being interrupted.
   */
  Object blockingGet() throws InterruptedException;

  /**
   * Test whether the future has been resolved, that is, the future is either set or failed.
   *
   * @return {@code true} if the future is resolved, {@code false} otherwise.
   */
  boolean isResolved();

  /**
   * Test whether the future has failed.
   *
   * @return {@code true} if the future is resolved and failed, {@code false} otherwise.
   */
  boolean isFailed();

  /**
   * Registers a callback for when the future is set. If the future has already been set, then it is executed
   * immediately from the caller thread.
   *
   * @param observer the callback.
   * @return this future object.
   */
  Future onSet(Observer observer);

  /**
   * Registers a callback for when the future fails. If the future has already been failed, then it is executed
   * immediately from the caller thread.
   *
   * @param observer the callback.
   * @return this future object.
   */
  Future onFail(Observer observer);

  /**
   * Simple interface for a future observer / callback.
   */
  @FunctionalInterface
  interface Observer {

    /**
     * Callback method.
     *
     * @param value the future value.
     */
    void apply(Object value);
  }
}
