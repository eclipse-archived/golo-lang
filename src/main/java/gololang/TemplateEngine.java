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

import java.lang.invoke.MethodHandle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {

  private final EvaluationEnvironment evaluationEnvironment = new EvaluationEnvironment();

  private final static Pattern PATTERN = Pattern.compile("<%(.*?)%>", Pattern.DOTALL);

  public MethodHandle compile(String template) {
    evaluationEnvironment.clearImports();
    String goloCode = templateToGolo(template);
//    System.out.println(goloCode);
    return (MethodHandle) evaluationEnvironment.def(goloCode);
  }

  private String templateToGolo(String template) {
    StringBuilder builder = new StringBuilder();
    String params = null;
    builder.append("  let _$result = java.lang.StringBuilder()\n");
    Matcher matcher = PATTERN.matcher(template);
    int startIndex = 0;
    while (matcher.find()) {
      String text = template.substring(startIndex, matcher.start());
      builder.append("  _$result: append(\"\"\"").append(text).append("\"\"\")\n");
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
