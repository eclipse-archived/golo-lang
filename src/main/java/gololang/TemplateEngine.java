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

import java.lang.invoke.MethodHandle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple template engine that turns text templates into Golo functions.
 * <p>
 * The template engine is similar to Ruby ERB or Java Server Pages. Golo code and directives can be embedded as follows:
 * <ul>
 * <li>{@code <% code %>} blocks contain any Golo code, and
 * <li>{@code <%= expression %>} outputs the evaluation of {@code expression}, and
 * <li>{@code <%@params foo, bar, baz %>} makes the template function take these parameter names, and
 * <li>{@code <%@import foo.bar.Baz %>} is equivalent to a {@code import} in a Golo module.
 * </ul>
 *
 * <p>
 * Here is a template example:
 * <pre>
 * <%@params persons %>
 * <% foreach (person in persons) { %>
 * Name: <%= person: name() %>
 * Email: <%= person: email() orIfNull "n/a" %>
 * <% } %>
 * </pre>
 *
 * The resulting function would take a single parameter {@code persons}. When no {@code @params} clause is being
 * specified, template functions are assumed to take a single {@code params} parameter.
 * <p>
 * It is important to note that this template engine performs no validation, either on the template itself or the
 * generated function code. One may however catch the {@link fr.insalyon.citi.golo.compiler.GoloCompilationException}
 * that {@link #compile(String)} may throw, and inspect the faulty code using
 * {@link fr.insalyon.citi.golo.compiler.GoloCompilationException#getSourceCode()} and
 * {@link fr.insalyon.citi.golo.compiler.GoloCompilationException#getProblems()}.
 */
public class TemplateEngine {

  private final EvaluationEnvironment evaluationEnvironment = new EvaluationEnvironment();

  private final static Pattern PATTERN = Pattern.compile("<%(.*?)%>", Pattern.DOTALL);

  /**
   * Compile a template into a function. The function takes parameters as specified using a {@code @params clause}, or
   * a single {@code params} argument if none exists.
   *
   * @param template the template code.
   * @return a compiled function that evaluates the template given parameters, and returns a {@link String}.
   * @throws fr.insalyon.citi.golo.compiler.GoloCompilationException
   *          if a compilation error occurs in the generated Golo code.
   */
  public MethodHandle compile(String template) {
    evaluationEnvironment.clearImports();
    String goloCode = templateToGolo(template);
    return (MethodHandle) evaluationEnvironment.def(goloCode);
  }

  /**
   * Generates the Golo code for a given template, but does not compile it.
   *
   * @param template the template code.
   * @return the corresponding Golo source code which may or may not be valid.
   */
  public String templateToGolo(String template) {
    StringBuilder builder = new StringBuilder();
    String params = null;
    builder.append("  let _$result = java.lang.StringBuilder()\n");
    Matcher matcher = PATTERN.matcher(template);
    int startIndex = 0;
    while (matcher.find()) {
      String text = template.substring(startIndex, matcher.start());
      int lowerBound = 0;
      int upperBound = text.length();
      if (text.startsWith("\"")) {
        lowerBound = 1;
        builder.append("  _$result: append(\"\\\"\")\n");
      }
      if (text.endsWith("\"")) {
        upperBound = text.length() - 1;
      }
      builder.append("  _$result: append(\"\"\"").append(text.substring(lowerBound, upperBound)).append("\"\"\")\n");
      if (text.endsWith("\"")) {
        builder.append("  _$result: append(\"\\\"\")\n");
      }
      String code = matcher.group();
      code = code.substring(2, code.length() - 2);
      if (code.startsWith("=")) {
        builder.append("  _$result: append(").append(code.substring(1)).append(")\n");
      } else if (code.startsWith("@params")) {
        params = "|" + code.substring(7).trim() + "| {\n";
      } else if (code.startsWith("@import")) {
        evaluationEnvironment.imports(code.substring(7).trim());
      } else {
        builder.append(code);
      }
      startIndex = matcher.end();
    }
    builder
        .append("\n  _$result: append(\"\"\"")
        .append(template.substring(startIndex))
        .append("\"\"\")\n")
        .append("  return _$result: toString()\n")
        .append("}\n");
    if (params == null) {
      params = "|params| {\n";
    }
    return params + builder.toString();
  }
}
