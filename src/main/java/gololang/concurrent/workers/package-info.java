/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Support for asynchronous, message-based processing of tasks.
 * <p>
 * Worker environments abstract from the need to deal with threads and message queues.
 * Worker functions are spawned, returning ports that can be used to dispatch messages.
 */
package gololang.concurrent.workers;