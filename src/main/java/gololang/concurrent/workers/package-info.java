/**
 * Support for asynchronous, message-based processing of tasks.
 * <p>
 * Worker environments abstract from the need to deal with threads and message queues.
 * Worker functions are spawned, returning ports that can be used to dispatch messages.
 */
package gololang.concurrent.workers;