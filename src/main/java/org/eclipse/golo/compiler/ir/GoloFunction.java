/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;
import java.util.Collection;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;
import static java.util.Arrays.asList;
import static org.eclipse.golo.compiler.ir.Builders.*;
import static java.util.Objects.requireNonNull;

public final class GoloFunction extends ExpressionStatement implements Scope {

  private static final SymbolGenerator SYMBOLS = new SymbolGenerator("function");

  private String name;
  private boolean isLocal = false;
  private Scope scope = Scope.MODULE;

  private final List<String> parameterNames = new LinkedList<>();
  private final List<String> syntheticParameterNames = new LinkedList<>();
  private int syntheticParameterCount = 0;
  private boolean varargs = false;
  private Block block = Builders.block();
  private boolean synthetic = false;
  private boolean decorator = false;
  private String syntheticSelfName = null;
  private String decoratorRef = null;
  private final LinkedList<Decorator> decorators = new LinkedList<>();

  public static enum Scope {
    MODULE, AUGMENT, CLOSURE
  }

  GoloFunction() {
    super();
  }

  @Override
  public GoloFunction ofAST(GoloASTNode n) {
    super.ofAST(n);
    return this;
  }

  // name -----------------------------------------------------------------------------------------
  public GoloFunction name(String n) {
    this.name = n;
    return this;
  }

  public String getName() {
    return name;
  }

  public boolean isMain() {
    return "main".equals(name) && getArity() == 1;
  }

  public boolean isModuleInit() {
    return GoloModule.MODULE_INITIALIZER_FUNCTION.equals(this.name);
  }

  public boolean isAnonymous() {
    return this.name == null;
  }

  // synthetic ------------------------------------------------------------------------------------
  public GoloFunction synthetic() {
    this.synthetic = true;
    return this;
  }

  public boolean isSynthetic() {
    return synthetic;
  }

  // decorator ------------------------------------------------------------------------------------
  public GoloFunction decorator() {
    return decorator(true);
  }

  public GoloFunction decorator(boolean d) {
    this.decorator = d;
    return this;
  }

  public boolean isDecorator() {
    return decorator;
  }

  // visibility -----------------------------------------------------------------------------------
  public GoloFunction local() {
    return local(true);
  }

  public GoloFunction local(boolean isLocal) {
    this.isLocal = isLocal;
    return this;
  }

  public boolean isLocal() {
    return this.isLocal;
  }

  // scope ----------------------------------------------------------------------------------------
  public GoloFunction inScope(GoloFunction.Scope s) {
    this.scope = s;
    return this;
  }

  public GoloFunction inAugment() {
    return inAugment(true);
  }

  public GoloFunction inAugment(boolean isInAugment) {
    if (isInAugment) {
      this.scope = Scope.AUGMENT;
    }
    return this;
  }

  public boolean isInAugment() {
    return Scope.AUGMENT.equals(scope);
  }

  public GoloFunction asClosure() {
    this.scope = Scope.CLOSURE;
    return this;
  }

  // block ----------------------------------------------------------------------------------------
  public GoloFunction block(Object... statements) {
    return this.block(Builders.block(statements));
  }

  public GoloFunction block(Block block) {
    this.block = requireNonNull(block);
    for (String param : parameterNames) {
      addParameterToBlockReferences(param);
    }
    makeParentOf(this.block);
    return this;
  }

  public Block getBlock() {
    return block;
  }

  public GoloFunction returns(Object expression) {
    this.block.add(Builders.returns(expression));
    return this;
  }

  // XXX: here or in sugar expansion ?
  public void insertMissingReturnStatement() {
    if (!this.block.hasReturn() && !this.isModuleInit()) {
      ReturnStatement missingReturnStatement = Builders.returns(constant(null));
      if (this.isMain()) {
        missingReturnStatement.returningVoid();
      }
      this.block.addStatement(missingReturnStatement);
    }
  }

  // parameters and varargs -----------------------------------------------------------------------
  public GoloFunction varargs(boolean isVarargs) {
    this.varargs = isVarargs;
    return this;
  }

  public GoloFunction varargs() {
    return this.varargs(true);
  }

  public boolean isVarargs() {
    return varargs;
  }

  public int getArity() {
    return parameterNames.size() + syntheticParameterCount;
  }

  public GoloFunction withParameters(String... names) {
    return withParameters(asList(names));
  }

  public GoloFunction withParameters(Collection<String> names) {
    for (String name : names) {
      addParameterToBlockReferences(name);
      this.parameterNames.add(name);
    }
    return this;
  }

  private void addParameterToBlockReferences(String name) {
    this.block.getReferenceTable().add(localRef(name));
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

  public String getSyntheticSelfName() {
    return syntheticSelfName;
  }

  public void setSyntheticSelfName(String syntheticSelfName) {
    this.syntheticSelfName = syntheticSelfName;
  }

  // decorators -----------------------------------------------------------------------------------
  public GoloFunction decoratedWith(Object... decorators) {
    for (Object deco : decorators) {
      if (deco instanceof Decorator) {
        this.addDecorator((Decorator) deco);
      } else {
        throw cantConvert("Decorator", deco);
      }
    }
    return this;
  }

  public boolean isDecorated() {
    return !decorators.isEmpty();
  }

  public String getDecoratorRef() {
    return decoratorRef;
  }

  public void addDecorator(Decorator decorator) {
    this.decorators.add(decorator);
    makeParentOf(decorator);
    if (this.decoratorRef == null) {
      this.decoratorRef = SYMBOLS.next(name + "_decorator");
    }
  }

  public List<Decorator> getDecorators() {
    return unmodifiableList(decorators);
  }

  public boolean hasDecorators() {
    return !decorators.isEmpty();
  }

  public GoloFunction createDecorator() {
    ExpressionStatement expr = refLookup("__$$_original");
    for (Decorator decorator : this.getDecorators()) {
      expr = decorator.wrapExpression(expr);
    }
    return functionDeclaration(this.decoratorRef)
      .decorator()
      .inScope(this.scope)
      .withParameters("__$$_original")
      .returns(expr);
  }

  //-----------------------------------------------------------------------------------------------
  public ClosureReference asClosureReference() {
    if (scope != Scope.CLOSURE) {
      throw new IllegalStateException("Can't get a closure reference of a non-closure function");
    }
    return new ClosureReference(this);
  }

  @Override
  public void relink(ReferenceTable table) {
    block.relink(table);
  }

  @Override
  public void relinkTopLevel(ReferenceTable table) {
    block.relinkTopLevel(table);
  }

  @Override
  public String toString() {
    return String.format("Function<name=%s, arity=%d, vararg=%s>",
        getName(),
        getArity(),
        isVarargs());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunction(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (Decorator deco : decorators) {
      deco.accept(visitor);
    }
    block.accept(visitor);
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }
}
