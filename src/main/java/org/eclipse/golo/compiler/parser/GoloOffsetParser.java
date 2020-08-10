/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.parser;

import java.io.InputStream;
import java.io.Reader;

/**
 * Golo parser extension allowing to track token offsets.
 */
public class GoloOffsetParser extends GoloParser {

  private void hookInputs() {
    jj_input_stream = new JavaOffsetCharStream(jj_input_stream);
    token_source = new GoloOffsetParserTokenManager(jj_input_stream);
  }

  public GoloOffsetParser(InputStream stream) {
    super(stream);
    hookInputs();
  }

  public GoloOffsetParser(InputStream stream, String encoding) {
    super(stream, encoding);
    hookInputs();
  }

  public GoloOffsetParser(Reader stream) {
    super(stream);
    hookInputs();
  }

  public GoloOffsetParser(GoloParserTokenManager tm) {
    super(tm);
    hookInputs();
  }
}
