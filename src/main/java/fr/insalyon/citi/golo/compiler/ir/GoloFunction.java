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

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class GoloFunction extends GoloElement {

  public static enum Visibility {
    PUBLIC, LOCAL
  }

  public static enum Scope {
    MODULE, AUGMENT, CLOSURE
  }

  private final String name;
  private final Visibility visibility;
  private final Scope scope;

  private List<String> parameterNames = new LinkedList<>();
  private List<String> syntheticParameterNames = new LinkedList<>();
  private int syntheticParameterCount = 0;
  private boolean varargs;
  private Block block;
  private boolean synthetic = false;
  private String syntheticSelfName = null;
  private LinkedList<Decorator> decorators = new LinkedList<>();

  public GoloFunction(String name, Visibility visibility, Scope scope) {
    this.name = name;
    this.visibility = visibility;
    this.scope = scope;
  }

  public Scope getScope() {
    return scope;
  }

  public int getSyntheticParameterCount() {
    return syntheticParameterCount;
  }

  public List<String> getParameterNames() {
    LinkedList<String> list = new LinkedList<>(syntheticParameterNames);
    list.addAll(parameterNames);
    return unmodifiableList(list);
  }

  public List<String> getSyntheticParameterNames() {
    return unmodifiableList(syntheticParameterNames);
  }

  public void setParameterNames(List<String> parameterNames) {
    this.parameterNames.addAll(parameterNames);
  }

  public void addSyntheticParameter(String name) {
    this.syntheticParameterNames.add(name);
    this.syntheticParameterCount = this.syntheticParameterCount + 1;
  }

  public void removeSyntheticParameter(String name) {
    this.syntheticParameterNames.remove(name);
    this.syntheticParameterCount = this.syntheticParameterCount - 1;
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  public String getName() {
    return name;
  }

  public boolean isSynthetic() {
    return synthetic;
  }

  public void setSynthetic(boolean synthetic) {
    this.synthetic = synthetic;
  }

  public String getSyntheticSelfName() {
    return syntheticSelfName;
  }

  public void setSyntheticSelfName(String syntheticSelfName) {
    this.syntheticSelfName = syntheticSelfName;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public int getArity() {
    return parameterNames.size() + syntheticParameterCount;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public Block getBlock() {
    return block;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

  public void addDecorator(Decorator decorator) {
    this.decorators.addLast(decorator);
  }

  public List<Decorator> getDecorators() {
    return decorators;
  }

  public boolean isMain() {
    return name.equals("main") && getArity() == 1;
  }

  public boolean isModuleInit() {
    return "<clinit>".equals(name);
  }

  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunction(this);
  }
}
