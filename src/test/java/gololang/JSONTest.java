/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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

  @Test
  public void dyobj_from_json_string() throws Throwable {
    Method dyobj_parse = moduleClass.getMethod("dyobj_from_json_string");

    DynamicObject obj = (DynamicObject) dyobj_parse.invoke(null);
    assertThat(obj.get("firstName"), is((Object) "Bob"));
    assertThat(obj.get("lastName"), is((Object) "Morane"));

    List<?> friends = (List<?>) obj.get("friends");
    DynamicObject bill = (DynamicObject) friends.get(0);
    assertThat(bill.get("firstName"), is((Object) "Bill"));
    assertThat(bill.get("lastName"), is((Object) "Ballantine"));

    DynamicObject aristide = (DynamicObject) friends.get(1);
    assertThat(aristide.get("firstName"), is((Object) "Aristide"));
    assertThat(aristide.get("lastName"), is((Object) "Clairembart"));

    DynamicObject sophia = (DynamicObject) friends.get(2);
    assertThat(sophia.get("firstName"), is((Object) "Sophia"));
    assertThat(sophia.get("lastName"), is((Object) "Paramount"));

    DynamicObject creator = (DynamicObject) obj.get("creator");
    assertThat(creator.get("firstName"), is((Object) "Henri"));
    assertThat(creator.get("lastName"), is((Object) "Vernes"));
  }

  @Test
  public void dyobjs_list_from_json_string() throws Throwable {
    Method dyobjs_parse = moduleClass.getMethod("dyobjs_list_from_json_string");

    List<?> objectsList = (List<?>) dyobjs_parse.invoke(null);
    DynamicObject message = (DynamicObject) objectsList.get(0);
    assertThat(message.get("message"), is((Object) "Hello World!"));
    DynamicObject bob = (DynamicObject) objectsList.get(1);
    assertThat(bob.get("firstName"), is((Object) "Bob"));
    assertThat(bob.get("lastName"), is((Object) "Morane"));
  }

  @Test
  public void dyobjs_list_from_maps_collection() throws Throwable {
    Method dyobjs_parse = moduleClass.getMethod("dyobjs_list_from_maps_collection");

    List<?> objectsList = (List<?>) dyobjs_parse.invoke(null);
    DynamicObject message = (DynamicObject) objectsList.get(0);
    assertThat(message.get("message"), is((Object) "Hello World!"));
    DynamicObject bob = (DynamicObject) objectsList.get(1);
    assertThat(bob.get("firstName"), is((Object) "Bob"));
    assertThat(bob.get("lastName"), is((Object) "Morane"));
  }
}
