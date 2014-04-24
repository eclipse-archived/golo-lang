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

package gololang;

import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static fr.insalyon.citi.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JSONTest {

  private static final String SRC = "src/test/resources/for-test/";
  private Class<?> moduleClass;

  @BeforeMethod
  public void load_module() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    moduleClass = compileAndLoadGoloModule(SRC, "json.golo");
  }

  @Test
  public void roundtrip() throws Throwable {
    Method roundtrip = moduleClass.getMethod("roundtrip");

    Object result = roundtrip.invoke(null);
    assertThat(result, Matchers.notNullValue());
    assertThat(result, Matchers.instanceOf(Tuple.class));

    Tuple tuple = (Tuple) result;
    assertThat(tuple.size(), is(2));

    assertThat(tuple.get(0), instanceOf(String.class));
    String str = (String) tuple.get(0);
    assertThat(str, containsString("69"));
    assertThat(str, containsString("\"name\":\"Somebody\""));
    assertThat(str, containsString("[\"Mr Bean\""));

    assertThat(tuple.get(1), instanceOf(Map.class));
    Map<?, ?> map = (Map<?,?>) tuple.get(1);
    assertThat(map, hasEntry((Object) "name", (Object) "Somebody"));
    assertThat(map, hasEntry((Object) "age", (Object) 69L));
    assertThat(map, hasKey((Object) "friends"));
    List<?> friends = (List<?>) map.get("friends");
    assertThat(friends, contains((Object) "Mr Bean", "John B", "Larry"));
  }
}
