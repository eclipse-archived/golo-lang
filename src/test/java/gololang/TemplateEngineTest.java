/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.testng.SkipException;
import org.testng.annotations.Test;

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
    FunctionReference tpl = engine.compile("Plop!");
    assertThat((String) tpl.handle().invoke(null), is("Plop!"));
  }

  @Test
  public void simple_value() throws Throwable {
    if (System.getenv("golo.bootstrapped") == null) {
      throw new SkipException("Golo is in a bootstrap build execution");
    }
    TemplateEngine engine = new TemplateEngine();
    FunctionReference tpl = engine.compile("<%= params: getOrElse(\"a\", \"n/a\")%>!");
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
    FunctionReference tpl = engine.compile(template);
    assertThat((String) tpl.handle().invoke(null), is("aaa"));
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
    FunctionReference tpl = engine.compile(template);
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
    FunctionReference tpl = engine.compile(template);
    assertThat(tpl.arity(), is(2));
    assertThat((String) tpl.invoke(1, 2), is("=3"));
  }

  @Test
  public void with_imports() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<%@import java.lang.Math %><%= max(1, 2) %>";
    FunctionReference tpl = engine.compile(template);
    assertThat((String) tpl.handle().invoke(null), is("2"));
  }

  @Test
  public void quote_delimiting_text() throws Throwable {
    TemplateEngine engine = new TemplateEngine();
    String template = "<%@params url %><a href=\"<%= url %>\">Link</a>";
    FunctionReference tpl = engine.compile(template);
    assertThat((String) tpl.invoke("http://foo.bar/"), is("<a href=\"http://foo.bar/\">Link</a>"));
  }
}
