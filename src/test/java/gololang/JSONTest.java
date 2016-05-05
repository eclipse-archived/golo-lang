/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import org.hamcrest.Matchers;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.eclipse.golo.internal.testing.TestUtils.compileAndLoadGoloModule;
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
    JSONAssert.assertEquals("{\"name\":\"Somebody\",\"age\":69,\"friends\":[\"Mr Bean\",\"John B\",\"Larry\"]}", str, true);

    assertThat(tuple.get(1), instanceOf(Map.class));
    Map<?, ?> map = (Map<?, ?>) tuple.get(1);
    assertThat(map, hasEntry((Object) "name", (Object) "Somebody"));
    assertThat(map, hasEntry((Object) "age", (Object) 69L));
    assertThat(map, hasKey((Object) "friends"));
    List<?> friends = (List<?>) map.get("friends");
    assertThat(friends, contains((Object) "Mr Bean", "John B", "Larry"));
  }

  @Test
  public void dyobj_stringify() throws Throwable {
    Method dyobj_stringify = moduleClass.getMethod("dyobj_stringify");
    String json = (String) dyobj_stringify.invoke(null);
    JSONAssert.assertEquals("{\"foo\":\"bar\",\"bar\":\"baz\",\"nested\":{\"a\":\"1\",\"b\":\"2\"}}", json, true);
  }

  @Test
  public void dyobj_stringify_mixin() throws Throwable {
    Method dyobj_stringify_mixin = moduleClass.getMethod("dyobj_stringify_mixin");
    String json = (String) dyobj_stringify_mixin.invoke(null);
    JSONAssert.assertEquals("{\"foo\":\"bar\",\"bar\":\"baz\",\"nested\":{\"a\":\"1\",\"b\":\"2\"}}", json, true);
  }

  @Test
  public void dyobj_parse() throws Throwable {
    Method dyobj_parse = moduleClass.getMethod("dyobj_parse");
    DynamicObject obj = (DynamicObject) dyobj_parse.invoke(null);
    assertThat(obj.get("a"), is((Object) "1"));
    assertThat(obj.get("b"), is((Object) "2"));
  }

  @Test
  public void struct_stringify() throws Throwable {
    Method struct_stringify = moduleClass.getMethod("struct_stringify");
    String json = (String) struct_stringify.invoke(null);
    JSONAssert.assertEquals("{\"name\":\"Mr Bean\",\"email\":\"mrbean@outlook.com\",\"age\":64}", json, true);
  }

  @Test
  public void struct_fromjson() throws Throwable {
    Method struct_fromjson = moduleClass.getMethod("struct_fromjson");
    GoloStruct struct = (GoloStruct) struct_fromjson.invoke(null);
    assertThat(struct.get("name"), is((Object) "Foo"));
    assertThat(struct.get("email"), is((Object) "foo@gmail.com"));
    assertThat(struct.get("age"), is((Object) 99L));
  }

  @Test
  public void stringify_mix_struct_and_dynobj() throws Throwable {
    Method stringify_mix_struct_and_dynobj = moduleClass.getMethod("stringify_mix_struct_and_dynobj");
    String json = (String) stringify_mix_struct_and_dynobj.invoke(null);
    JSONAssert.assertEquals("{\"a\":\"1\",\"b\":{\"name\":\"Mr Bean\",\"email\":\"mrbean@outlook.com\",\"age\":64}}", json, true);
  }
}
