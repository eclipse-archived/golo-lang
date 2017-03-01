/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.parser;

import java.io.IOException;

/**
 * JavaCharStream extension allowing to track token offsets.
 */
public class JavaOffsetCharStream extends JavaCharStream {

  private int beginOffset;

  private int currentOffset;

  public JavaOffsetCharStream(final JavaCharStream delegate) {
    super(delegate.inputStream);
  }

  @Override
  public char BeginToken() throws IOException {
    /*
     * JavaCC use a pre fetch buffer and may not call readChar causing our
     * offset not to be updated
     */
    if (inBuf > 0) {
      currentOffset++;
    }
    char c = super.BeginToken();
    beginOffset = currentOffset;
    return c;
  }

  @Override
  public char readChar() throws IOException {
    char c = super.readChar();
    currentOffset++;
    return c;
  }

  @Override
  public void backup(int amount) {
    super.backup(amount);
    currentOffset -= amount;
  }

  public int getBeginOffset() {
    return beginOffset;
  }

  public int getCurrentOffset() {
    return currentOffset;
  }
}
