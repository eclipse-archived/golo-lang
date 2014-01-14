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

package fr.insalyon.citi.golo.compiler.ir;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractInvocation extends ExpressionStatement {

  private final String name;
  private final List<ExpressionStatement> arguments = new LinkedList<>();
  private final List<FunctionInvocation> anonymousFunctionInvocations = new LinkedList<>();

  public AbstractInvocation(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addArgument(ExpressionStatement argument) {
    arguments.add(argument);
  }

  public List<ExpressionStatement> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public int getArity() {
    return arguments.size();
  }

  public void addAnonymousFunctionInvocation(FunctionInvocation invocation) {
    anonymousFunctionInvocations.add(invocation);
  }

  public List<FunctionInvocation> getAnonymousFunctionInvocations() {
    return Collections.unmodifiableList(anonymousFunctionInvocations);
  }
}
