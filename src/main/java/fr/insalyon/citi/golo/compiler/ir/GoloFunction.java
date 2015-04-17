/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class GoloFunction extends ExpressionStatement {

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
  private boolean decorator = false;
  private String syntheticSelfName = null;
  private String decoratorRef = null;
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

  public boolean isDecorated() {
    return !decorators.isEmpty();
  }

  public boolean isDecorator() {
    return decorator;
  }

  public void setDecorator(boolean decorator) {
    this.decorator = decorator;
  }

  public String getSyntheticSelfName() {
    return syntheticSelfName;
  }

  public void setSyntheticSelfName(String syntheticSelfName) {
    this.syntheticSelfName = syntheticSelfName;
  }

  public String getDecoratorRef() {
    return decoratorRef;
  }

  public void setDecoratorRef(String decoratorRef) {
    this.decoratorRef = decoratorRef;
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
