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

class Monomorphic {

    static def run() {
        def max = 5000000
        def result = null
        for (def i = 0; i < max; i = i + 1) {
            result = i.toString()
        }
        return result
    }

    static def run_safenav() {
        def max = 5000000
        def result = null
        for (def i = 0; i < max; i = i + 1) {
            result = i?.toString()
        }
        return result
    }

    static def run_safenav_with_nulls() {
        def max = 5000000
        def obj = null
        def random = new java.util.Random()
        def result = null
        for (def i = 0; i < max; i = i + 1) {
            obj = random.nextBoolean() ? null : i
            result = obj?.toString()
        }
        return result
    }
}
