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

package fr.insalyon.citi.golo.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Metadata {

  public static final String VERSION;
  public static final String TIMESTAMP;

  static {
    Properties props = new Properties();
    InputStream inputStream = Metadata.class.getResourceAsStream("/metadata.properties");
    try {
      props.load(inputStream);
      VERSION = props.getProperty("version");
      TIMESTAMP = props.getProperty("timestamp");
    } catch (IOException e) {
      throw new AssertionError("Could not load metadata.properties from the current classpath.");
    }
  }
}
