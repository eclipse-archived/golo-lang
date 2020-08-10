/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Support for asynchronous, message-based processing of tasks.
 * <p>
 * Worker environments abstract from the need to deal with threads and message queues.
 * Worker functions are spawned, returning ports that can be used to dispatch messages.
 */
package gololang.concurrent.workers;
