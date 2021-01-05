/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Locale;
import gololang.ir.GoloElement;
import gololang.ir.GoloModule;

/**
 * Functions to display various localized messages.
 */
public final class Messages {

  private static final boolean ANSI = !System.getProperty("os.name").contains("Windows");
  private static final boolean color = (System.console() != null);

  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages", Locale.getDefault());

  private static final String ERROR = "\u001B[31m"; // red
  private static final String INFO = "\u001B[34m";  // blue
  private static final String WARNING = "\u001B[33m"; // yellow
  private static final String STACK = "\u001B[36m"; // cyan

  private Messages() {
    // utility class
  }

  private static boolean withColor() {
    return ANSI && color;
  }

  /**
   * Format a localized message.
   */
  public static String message(String key, Object... values) {
    return MessageFormat.format(MESSAGES.getString(key), values);
  }

  /**
   * Return a localized message.
   */
  public static String message(String key) {
    return MESSAGES.getString(key);
  }

  public static String prefixed(String prefix, String message) {
    return prefixed(prefix, message, null);
  }

  public static String prefixed(String prefix, String message, String color) {
    if (!withColor() || color == null) {
      return String.format("[%s] %s", MESSAGES.getString(prefix), message);
    }
    return String.format("[%s%s\u001B[0m] %s", color, MESSAGES.getString(prefix), message);
  }

  public static void printPrefixed(String prefix, String message, String color) {
    System.err.println(prefixed(prefix, message, color));
  }

  public static void printPrefixedWithPosition(String prefix, Object message, GoloElement<?> node, String color) {
    String source = "unknown";
    int line = 0;
    int column = 0;
    if (node != null) {
      GoloModule mod = node.enclosingModule();
      if (mod != null) {
        source = mod.sourceFile();
      }
      line = node.positionInSourceCode().getStartLine();
      column = node.positionInSourceCode().getStartColumn();
    }
    printPrefixed(
        prefix,
        node == null ? String.valueOf(message) : String.format("%s (%s {l=%s,c=%s})",
          message, source, line, column),
        color);
  }

  /**
   * Prints an error message to standard error.
   */
  public static void error(Object message, String prefix) {
    printPrefixed("error", prefix + String.valueOf(message), ERROR);
  }

  /**
   * Prints an error message to standard error.
   */
  public static void error(Object message) {
    error(message, "");
  }

  /**
   * Prints an error message to standard error with information from given IR node.
   */
  public static void error(Object message, GoloElement<?> node) {
    printPrefixedWithPosition("error", message, node, ERROR);
  }


  /**
   * Prints a warning to standard error.
   */
  public static void warning(Object message) {
    printPrefixed("warning", String.valueOf(message), WARNING);
  }

  /**
   * Prints a warning to standard error with information from given IR node.
   */
  public static void warning(Object message, GoloElement<?> node) {
    printPrefixedWithPosition("warning", message, node, WARNING);
  }

  /**
   * Prints an info message to standard error.
   */
  public static void info(Object message) {
    printPrefixed("info", String.valueOf(message), INFO);
  }

  /**
   * Prints an info message to standard error with information from given IR node.
   */
  public static void info(Object message, GoloElement<?> node) {
    printPrefixedWithPosition("info", message, node, INFO);
  }

  /**
   * Prints an error stack trace to standard error.
   */
  public static void printStackTrace(Throwable e) {
    if (withColor()) {
      System.err.print(STACK);
    }
    e.printStackTrace();
    if (withColor()) {
      System.err.println("\u001B[0m");
    }
  }



}
