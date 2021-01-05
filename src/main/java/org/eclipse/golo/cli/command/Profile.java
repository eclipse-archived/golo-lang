/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.emptyList;


final class Profile {
  private String defaultFileName;
  private String label;
  private boolean runnable = false;
  private List<Path> directories = emptyList();

  public Profile defaultFileName(String fileName) {
    this.defaultFileName = fileName;
    return this;
  }

  public String defaultFileName() {
    return this.defaultFileName;
  }

  public String label() {
    return this.label;
  }

  public Profile label(String label) {
    this.label = label;
    return this;
  }

  public boolean isRunnable() {
    return this.runnable;
  }

  public Profile runnable() {
    this.runnable = true;
    return this;
  }

  public List<Path> directories() {
    return unmodifiableList(this.directories);
  }

  public Profile directories(Path... directories) {
    this.directories = Arrays.asList(directories);
    return this;
  }
}


