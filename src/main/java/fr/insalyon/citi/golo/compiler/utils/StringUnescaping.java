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

package fr.insalyon.citi.golo.compiler.utils;

public class StringUnescaping {

  private static final String[] ESCAPE_STRINGS = {
      String.valueOf('\n'),
      String.valueOf('\t'),
      String.valueOf('\b'),
      String.valueOf('\r'),
      String.valueOf('\f'),
      String.valueOf('\''),
      String.valueOf('"'),
      String.valueOf('\\')
  };

  private static final String[] SEQS = {
      "\\n",
      "\\t",
      "\\b",
      "\\r",
      "\\f",
      "\\'",
      "\\\"",
      "\\\\"
  };

  public static String unescape(String str) {
    String result = str;
    for (int i = 0; i < ESCAPE_STRINGS.length; i++) {
      result = result.replace(SEQS[i], ESCAPE_STRINGS[i]);
    }
    return result;
    // TODO: this is a rather inefficient algorithm...
  }
}
