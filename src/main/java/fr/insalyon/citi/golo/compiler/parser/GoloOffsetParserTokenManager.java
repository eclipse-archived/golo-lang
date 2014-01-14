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
      t.startOffset = ((JavaOffsetCharStream)input_stream).getBeginOffset() - 1;
      t.endOffset = ((JavaOffsetCharStream)input_stream).getCurrentOffset();
    }
    return t;
  }
}
