/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package dispatch;

public class TriMorphic {

  public static Object run() {
    Object[] data = new Object[]{
        "foo",
        666,
        "bar",
        999,
        "plop",
        "da",
        "plop",
        "for",
        "ever",
        1,
        2,
        3,
        4,
        5,
        6,
        new Object(),
        new Object(),
        new Object(),
        new Object(),
    };
    Object result = null;
    for (int i = 0; i < 200000; i++) {
      for (int j = 0; j < data.length; j++) {
        result = data[j].toString();
      }
    }
    return result;
  }
}
