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
  public static interface Observer {

    /**
     * Callback method.
     *
     * @param value the future value.
     */
    void apply(Object value);
  }
}
