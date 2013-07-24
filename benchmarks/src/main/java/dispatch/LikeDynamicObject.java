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

import java.util.HashMap;
import java.util.Random;

public class LikeDynamicObject {

  static class IntegerProvider {

    public Object next(Random random) {
      return random.nextInt();
    }
  }

  public static Object run() throws Throwable {
    Random random = new Random();
    HashMap<String, Object> obj = new HashMap<>();
    obj.put("acc", 0);
    obj.put("rand", new IntegerProvider());

    for (int i = 0; i < 5_000_000; i++) {
      int accValue = (Integer) obj.get("acc");
      IntegerProvider provider = (IntegerProvider) obj.get("rand");
      obj.put("acc", accValue + ((Integer) provider.next(random)));
    }
    return obj.get("acc");
  }
}
