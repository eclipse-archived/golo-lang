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

package gololang;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class EvaluationEnvironment {

  private final GoloClassLoader goloClassLoader = new GoloClassLoader();
  private final List<String> imports = new LinkedList<>();

  private static String anonymousFilename() {
    return "$Anonymous$_" + System.nanoTime() + ".golo";
  }

  private static String anonymousModuleName() {
    return "module anonymous" + System.nanoTime();
  }

  public EvaluationEnvironment imports(String head, String... tail) {
    imports.add(head);
    Collections.addAll(imports, tail);
    return this;
  }

  public EvaluationEnvironment clearImports() {
    imports.clear();
    return this;
  }

  public Object moduleFile(String source) {
    try (InputStream in = new ByteArrayInputStream(source.getBytes())) {
      return goloClassLoader.load(anonymousFilename(), in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Object anonymousModule(String source) {
    return moduleFile(anonymousModuleName() + "\n\n" + source);
  }

  public Object def(String source) {
    return loadAndRun("return " + source, "$_code");
  }

  public Object asFunction(String source, String... argumentNames) {
    return loadAndRun(source, "$_code_ref", argumentNames);
  }

  public Object run(String source) {
    return loadAndRun(source, "$_code");
  }

  public Object run(String source, Map<String, Object> context) {
    StringBuilder builder = new StringBuilder();
    for (String param : context.keySet()) {
      builder
          .append("let ")
          .append(param)
          .append(" = $_env: get(\"")
          .append(param)
          .append("\")\n");
    }
    builder.append(source);
    return loadAndRun(builder.toString(), "$_code", new String[]{"$_env"}, new Object[]{context});
  }

  private Class<?> wrapAndLoad(String source, String... argumentNames) {
    StringBuilder builder = new StringBuilder()
        .append(anonymousModuleName())
        .append("\n");
    for (String importSymbol : imports) {
      builder.append("import ").append(importSymbol).append("\n");
    }
    builder.append("\nfunction $_code = ");
    if (argumentNames.length > 0) {
      builder.append("| ");
      final int lastIndex = argumentNames.length - 1;
      for (int i = 0; i < argumentNames.length; i++) {
        builder.append(argumentNames[i]);
        if (i < lastIndex) {
          builder.append(", ");
        }
      }
      builder.append(" |");
    }
    builder
        .append(" {\n")
        .append(source)
        .append("\n}\n\n")
        .append("function $_code_ref = -> ^$_code\n\n");
    return (Class<?>) moduleFile(builder.toString());
  }

  private Object loadAndRun(String source, String target, String... argumentNames) {
    try {
      Class<?> module = wrapAndLoad(source, argumentNames);
      return module.getMethod(target).invoke(null);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Object loadAndRun(String source, String target, String[] argumentNames, Object[] arguments) {
    try {
      Class<?> module = wrapAndLoad(source, argumentNames);
      Class<?>[] type = new Class<?>[argumentNames.length];
      Arrays.fill(type, Object.class);
      return module.getMethod(target, type).invoke(null, arguments);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
