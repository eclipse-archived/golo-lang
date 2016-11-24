/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

  static {
    Properties props = new Properties();
    try (InputStream inputStream = Metadata.class.getResourceAsStream("/metadata.properties")) {
      props.load(inputStream);
      VERSION = props.getProperty("version");
      TIMESTAMP = props.getProperty("timestamp");
    } catch (IOException e) {
      throw new AssertionError("Could not load metadata.properties from the current classpath.");
    }
  }
}
