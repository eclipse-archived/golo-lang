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

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * Internal class to init and manage VCS for new projects.
 */
class VCS {

  public static final class InitException extends Exception {
    private int status;

    InitException(int status, String message) {
      super(message);
      this.status = status;
    }

    int getStatus() {
      return status;
    }
  }

  private static final long TIMEOUT = 5L;

  private ProcessBuilder command = new ProcessBuilder().inheritIO();
  private Path ignore;
  private Path destination;
  private List<String> toIgnore = new LinkedList<>();

  public VCS command(String... commands) {
    command.command(commands);
    return this;
  }

  public VCS directory(Path dest) {
    command.directory(dest.toFile());
    this.destination = dest;
    return this;
  }

  public void init() throws IOException, InterruptedException, InitException {
    command.directory().mkdirs();
    Process p = command.start();
    if (!p.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
      p.destroyForcibly();
      throw new InitException(1, "timeout");
    }
    int status = p.exitValue();
    if (status > 0) {
      throw new InitException(status, null);
    }
  }

  public VCS ignoreFilename(String filename) {
    this.ignore = Paths.get(filename);
    return this;
  }

  public Path ignoreFile() {
    return ignore;
  }

  public VCS ignore(String... patterns) {
    for (String p : patterns) {
      toIgnore.add(p);
    }
    return this;
  }

  public VCS ignore(Collection<String> patterns) {
    toIgnore.addAll(patterns);
    return this;
  }

  public void createIgnoreFile() throws FileNotFoundException, UnsupportedEncodingException, IOException {
    if (ignore == null) { return; }
    Files.createDirectories(destination);
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(destination.resolve(ignore), StandardCharsets.UTF_8))) {
      for (String pattern : toIgnore) {
        writer.println(pattern);
      }
    }
  }

  public String toString() {
    return String.format("%s (%s)", command.command().get(0), command.directory());
  }
}

