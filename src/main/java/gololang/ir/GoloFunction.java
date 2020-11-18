/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import java.lang.invoke.MethodType;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.golo.compiler.SymbolGenerator;

import static java.util.Collections.unmodifiableList;
import static java.util.Arrays.asList;

/**
 * Represents a function declaration.
 *
 * <p>Such as
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * function foo = |x| -> x + 1
 * </code></pre>
 */
public final class GoloFunction extends ExpressionStatement<GoloFunction> implements BlockContainer<GoloFunction>, ToplevelGoloElement, NamedElement {

  private static final SymbolGenerator SYMBOLS = new SymbolGenerator("golo.ir.function");

  private String name;
  private boolean isLocal = false;
  private Scope scope = Scope.MODULE;

  private final List<String> parameterNames = new LinkedList<>();
  private final List<String> syntheticParameterNames = new LinkedList<>();
  private boolean varargs = false;
  private Block block;
  private boolean synthetic = false;
  private boolean decorator = false;
  private boolean macro = false;
  private boolean special = false;
  private boolean contextual = false;
  private String syntheticSelfName = null;
  private String decoratorRef = null;
  private final LinkedList<Decorator> decorators = new LinkedList<>();

  public enum Scope {
    MODULE, AUGMENT, CLOSURE
  }

  protected GoloFunction self() { return this; }

  private GoloFunction() {
    super();
    this.block(Block.empty());
  }

  /**
   * Copy constructor.
   * <p>
   * make a partial copy of the given function: the properties are copied, but children node are not
   */
  private GoloFunction(GoloFunction function) {
    this.name = function.name;
    this.isLocal = function.isLocal;
    this.scope = function.scope;
    this.varargs = function.varargs;
    this.synthetic = function.synthetic;
    this.decorator = function.decorator;
    this.macro = function.macro;
    this.special = function.special;
    this.contextual = function.contextual;
    this.syntheticSelfName = function.syntheticSelfName;
    this.decoratorRef = function.decoratorRef;
    this.parameterNames.addAll(function.parameterNames);
    this.syntheticParameterNames.addAll(function.syntheticParameterNames);
    this.documentation(function.documentation());
    this.positionInSourceCode(function.positionInSourceCode());
  }

  /**
   * Creates a function declaration.
   *
   * <p>Typical usage:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * function("foo").withParameters("x").returns(plus(ReferenceLookup.of("x"), constant(1)))
   * </code></pre>
   * creates
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * function foo = |x| -> x + 1
   * </code></pre>
   */
  public static GoloFunction function(Object name) {
    if (name instanceof GoloFunction) {
      return new GoloFunction((GoloFunction) name);
    }
    if (name == null) {
      return new GoloFunction();
    }
    return new GoloFunction().name(name.toString());
  }

  // name -----------------------------------------------------------------------------------------
  public GoloFunction name(String n) {
    this.name = n;
    return this;
  }

  @Override
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
  public GoloFunction synthetic(boolean s) {
    this.synthetic = s;
    return this;
  }

  public GoloFunction synthetic() {
    return synthetic(true);
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
  public GoloFunction inScope(Scope s) {
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

  public boolean isInModule() {
    return Scope.MODULE.equals(scope);
  }

  public GoloFunction asClosure() {
    this.scope = Scope.CLOSURE;
    return this;
  }

  // block ----------------------------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  @Override
  public GoloFunction block(Object block) {
    this.block = makeParentOf(Block.of(block));
    for (String param : parameterNames) {
      addParameterToBlockReferences(param);
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Block getBlock() {
    return block;
  }

  public GoloFunction returns(Object expression) {
    this.block.add(ReturnStatement.of(expression));
    return this;
  }

  public void insertMissingReturnStatement() {
    if (!this.block.hasReturn() && !this.isModuleInit()) {
      if (this.isMain()) {
        this.block.add(ReturnStatement.empty().synthetic());
      } else {
        this.block.add(ReturnStatement.of(null).synthetic());
      }
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

  public GoloFunction withParameters(Object... names) {
    return withParameters(asList(names));
  }

  public GoloFunction withParameters(Collection<?> names) {
    for (Object name : names) {
      addParameterToBlockReferences(name.toString());
      this.parameterNames.add(name.toString());
    }
    return this;
  }

  private void addParameterToBlockReferences(String name) {
    this.block.getReferenceTable().add(LocalReference.of(name));
  }

  public int getSyntheticParameterCount() {
    return syntheticParameterNames.size();
  }

  public List<String> getParameterNames() {
    LinkedList<String> list = new LinkedList<>(syntheticParameterNames);
    list.addAll(parameterNames);
    return list;
  }

  public Collection<String> getSyntheticParameterNames() {
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

  public LocalReference getSyntheticSelfReference() {
    return block.getReferenceTable().get(syntheticSelfName);
  }

  public void setSyntheticSelfName(String name) {
    if (syntheticParameterNames.contains(name)) {
      this.syntheticParameterNames.remove(name);
      this.syntheticSelfName = name;
    }
  }

  public void captureClosedReference() {
    if (synthetic && syntheticSelfName != null) {
      block.prepend(AssignmentStatement.create(getSyntheticSelfReference(), asClosureReference(), true));
    }
  }

  // macro ----------------------------------------------------------------------------------------
  public GoloFunction asMacro(boolean value) {
    this.macro = value;
    return this;
  }

  public GoloFunction asMacro() {
    return asMacro(true);
  }

  public boolean isMacro() {
    return this.macro;
  }

  public boolean isSpecialMacro() {
    return this.special;
  }

  public GoloFunction special(boolean value) {
    this.special = value;
    return this;
  }

  public boolean isContextualMacro() {
    return this.contextual;
  }

  public GoloFunction contextual(boolean value) {
    this.contextual = value;
    return this;
  }

  // decorators -----------------------------------------------------------------------------------
  /**
   * Adds decorators to this function.
   *
   * <p>The objects are converted into a decorator if needed.
   */
  public GoloFunction decoratedWith(Object... decorators) {
    for (Object deco : decorators) {
      this.addDecorator(Decorator.of(deco));
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
    this.decorators.add(makeParentOf(decorator));
  }

  public void removeDecorator(Decorator decorator) {
    this.decorators.remove(decorator);
  }

  public List<Decorator> getDecorators() {
    return unmodifiableList(decorators);
  }

  public GoloFunction createDecorator() {
    ExpressionStatement<?> expr = ReferenceLookup.of("__$$_original");
    for (Decorator decorator : this.getDecorators()) {
      expr = decorator.wrapExpression(expr);
    }
    this.decoratorRef = SYMBOLS.next(name + "_decorator");
    return function(this.decoratorRef)
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


  /**
   * Return a new function with the same signature as this one.
   * <p>
   * Can be used to generate a wrapper for a given function for instance.
   */
  public GoloFunction sameSignature() {
    return function(name)
      .local(isLocal)
      .inScope(scope)
      .withParameters(parameterNames)
      .varargs(varargs)
      .synthetic(true);
  }

  /**
   * Return the parameters of this function wrapped in a {@code ReferenceLookup}.
   */
  public Object[] parametersAsRefs() {
    return parameterNames.stream().map(ReferenceLookup::of).toArray();
  }

  @Override
  public String toString() {
    return String.format("Function{name=%s, arity=%d, vararg=%s, synthetic=%s, self=%s, macro=%s}",
        getName(),
        getArity(),
        isVarargs(),
        synthetic,
        syntheticSelfName,
        isMacro());
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunction(this);
  }

  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>(decorators);
    children.add(block);
    return children;
  }

  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
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

  public MethodType getMethodType() {
    if (this.isMain()) {
      return MethodType.methodType(void.class, String[].class);
    }
    if (this.isModuleInit()) {
      return MethodType.methodType(void.class);
    }
    if (this.isMacro()) {
      MethodType signature = MethodType.methodType(GoloElement.class);
      int arity = this.getArity() - (this.isVarargs() ? 1 : 0);
      int i = 0;
      if (this.isContextualMacro()) {
        signature = signature.appendParameterTypes(gololang.ir.AbstractInvocation.class);
        i++;
      }
      if (this.isSpecialMacro()) {
        signature = signature.appendParameterTypes(org.eclipse.golo.compiler.macro.MacroExpansionIrVisitor.class);
        i++;
      }
      while (i < arity) {
        signature = signature.appendParameterTypes(GoloElement.class);
        i++;
      }
      if (this.isVarargs()) {
        signature = signature.appendParameterTypes(GoloElement[].class);
      }
      return signature;
    }
    if (this.isVarargs()) {
      return MethodType.genericMethodType(this.getArity() - 1, true);
    }
    return MethodType.genericMethodType(this.getArity());
  }
}
