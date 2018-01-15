/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

/**
 * TokenManager extension allowing to track token offsets.
 */
public class GoloOffsetParserTokenManager extends GoloParserTokenManager {

  public GoloOffsetParserTokenManager(JavaCharStream stream) {
    super(new JavaOffsetCharStream(stream));
  }

  public GoloOffsetParserTokenManager(JavaCharStream stream, int lexState) {
    super(new JavaOffsetCharStream(stream), lexState);
  }

  @Override
  protected Token jjFillToken() {
    Token t = super.jjFillToken();
    if (input_stream instanceof JavaOffsetCharStream) {
      t.startOffset = ((JavaOffsetCharStream) input_stream).getBeginOffset() - 1;
      t.endOffset = ((JavaOffsetCharStream) input_stream).getCurrentOffset();
    }
    return t;
  }
}
