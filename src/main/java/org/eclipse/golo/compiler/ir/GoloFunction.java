/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.ir;

import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Collections.unmodifiableList;
import static java.util.Arrays.asList;
import java.util.Objects;
import static org.eclipse.golo.compiler.ir.Builders.*;
import static java.util.Objects.requireNonNull;

public final class GoloFunction extends ExpressionStatement {

  private static final SymbolGenerator SYMBOLS = new SymbolGenerator("function");

  private String name;
  private boolean isLocal = false;
  private Scope scope = Scope.MODULE;

  private final List<String> parameterNames = new LinkedList<>();
  private final List<String> syntheticParameterNames = new LinkedList<>();
  private boolean varargs = false;
  private Block block;
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
    block = Builders.block();
    makeParentOf(block);
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

  public void insertMissingReturnStatement() {
    if (!this.block.hasReturn() && !this.isModuleInit()) {
      ReturnStatement missingReturnStatement = Builders.returns(constant(null)).synthetic();
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
    return parameterNames.size() + syntheticParameterNames.size();
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
    return syntheticParameterNames.size();
  }

  public List<String> getParameterNames() {
    LinkedList<String> list = new LinkedList<>(syntheticParameterNames);
    list.addAll(parameterNames);
    return unmodifiableList(list);
  }

  public List<String> getSyntheticParameterNames() {
    return unmodifiableList(syntheticParameterNames);
  }

  public void addSyntheticParameters(Set<String> names) {
    Set<String> existing = new HashSet<>(getParameterNames());
    for (String name : names) {
      if (!existing.contains(name) && !name.equals(syntheticSelfName)) {
        LocalReference ref = block.getReferenceTable().get(name);
        if (ref == null || !ref.isModuleState()) {
          this.syntheticParameterNames.add(name);
        }
      }
    }
  }

  public String getSyntheticSelfName() {
    return syntheticSelfName;
  }

  public void setSyntheticSelfName(String name) {
    if (syntheticParameterNames.contains(name)) {
      this.syntheticParameterNames.remove(name);
      this.syntheticSelfName = name;
    }
  }

  public void captureClosedReference() {
    if (synthetic && syntheticSelfName != null) {
      LocalReference self = block.getReferenceTable().get(syntheticSelfName);
      ClosureReference closureReference = asClosureReference();
      closureReference.updateCapturedReferenceNames();
      block.prependStatement(Builders.define(self).as(closureReference));
    }
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
    this.decoratorRef = SYMBOLS.next(name + "_decorator");
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
  public String toString() {
    return String.format("Function<name=%s, arity=%d, vararg=%s, synthetic=%s, self=%s>",
        getName(),
        getArity(),
        isVarargs(),
        synthetic,
        syntheticSelfName);
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

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.getArity(), this.varargs);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GoloFunction other = (GoloFunction) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (this.varargs != other.varargs) {
      return false;
    }
    return this.getArity() == other.getArity();
  }
}
