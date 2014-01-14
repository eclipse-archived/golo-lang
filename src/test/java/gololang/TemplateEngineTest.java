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

import org.testng.SkipException;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TemplateEngineTest {

  @Test
  public void simple_string() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    MethodHandle tpl = engine.compile("Plop!");
    assertThat((String) tpl.invoke(null), is("Plop!"));
  }

  @Test
  public void simple_value() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    TemplateEngine engine = new TemplateEngine();
    MethodHandle tpl = engine.compile("<%= params: getOrElse(\"a\", \"n/a\")%>!");
    assertThat((String) tpl.invoke(Collections.emptyMap()), is("n/a!"));
    assertThat((String) tpl.invoke(new TreeMap<String, String>() {
      {
        put("a", "Plop!");
      }
    }), is("Plop!!"));
  }

  @Test
  public void simple_repeat() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<% foreach (i in range(0, 3)) { %>a<% } %>";
    MethodHandle tpl = engine.compile(template);
    assertThat((String) tpl.invoke(null), is("aaa"));
  }

  @Test
  public void render_people() throws Throwable {
    HashMap<String, Object> params = new HashMap<String, Object>() {
      {
        put("people", Arrays.asList("Julien", "Mr Bean", "Bob LesPonges"));
      }
    };
    String template = "People:\n" +
        "<% foreach (p in params: get(\"people\")) { %>- <%= p %>\n" +
        "<% } %>\n";
    TemplateEngine engine = new TemplateEngine();
    MethodHandle tpl = engine.compile(template);
    assertThat((String) tpl.invoke(params), is(
        "People:\n" +
        "- Julien\n" +
        "- Mr Bean\n" +
        "- Bob LesPonges\n\n"));
  }

  @Test
  public void with_params() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<%@params foo, bar %>=<%= foo + bar %>";
    MethodHandle tpl = engine.compile(template);
    assertThat(tpl.type().parameterCount(), is(2));
    assertThat((String) tpl.invoke(1, 2), is("=3"));
  }

  @Test
  public void with_imports() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<%@import java.lang.Math %><%= max(1, 2) %>";
    MethodHandle tpl = engine.compile(template);
    assertThat((String) tpl.invoke(null), is("2"));
  }

  @Test
  public void quote_delimiting_text() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<%@params url %><a href=\"<%= url %>\">Link</a>";
    MethodHandle tpl = engine.compile(template);
    assertThat((String) tpl.invoke("http://foo.bar/"), is("<a href=\"http://foo.bar/\">Link</a>"));
  }
}
