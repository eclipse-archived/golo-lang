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

package fr.insalyon.citi.golo.compiler.parser;

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
