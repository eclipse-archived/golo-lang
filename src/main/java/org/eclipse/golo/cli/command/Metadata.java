/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Metadata {

  private Metadata() {
    // purely static class
  }

  public static final String VERSION;
  public static final String TIMESTAMP;
  public static final String GUIDE_BASE;

  static {
    Properties props = new Properties();
    try (InputStream inputStream = Metadata.class.getResourceAsStream("/metadata.properties")) {
      props.load(inputStream);
      VERSION = props.getProperty("version");
      TIMESTAMP = props.getProperty("timestamp");
      GUIDE_BASE = props.getProperty("guide-url");
    } catch (IOException e) {
      throw new AssertionError("Could not load metadata.properties from the current classpath.");
    }
  }
}
