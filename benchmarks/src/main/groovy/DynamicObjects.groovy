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

class DynamicObjects {

    static def run() {
        def obj = new Expando()
        obj.acc = 0
        def random = new java.util.Random()
        obj.rand = {
            return random.nextInt()
        }
        for (int i = 0; i < 5_000_000; i++) {
            obj.acc = obj.acc + obj.rand()
        }
        return obj.acc
    }
}

