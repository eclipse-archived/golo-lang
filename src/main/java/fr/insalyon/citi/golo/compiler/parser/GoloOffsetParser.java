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
