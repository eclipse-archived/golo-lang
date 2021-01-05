/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler.macro;

import gololang.ir.*;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.PositionInSourceCode;
import org.eclipse.golo.compiler.StopCompilationException;

import java.util.*;
import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.eclipse.golo.compiler.GoloCompilationException.Problem.Type.*;
import static org.eclipse.golo.cli.command.Metadata.GUIDE_BASE;
import static gololang.Messages.message;
import static gololang.Messages.info;


/**
 * Visitor to expand macro calls.
 * <p>
 * This visitor replace the {@code MacroInvocation} nodes with the result of the macro
 * expansion.
 */
public final class MacroExpansionIrVisitor extends AbstractGoloIrVisitor {

  public static final class RecursionLimitException extends RuntimeException {
    RecursionLimitException(String message) {
      super(message);
    }

    static RecursionLimitException of(int limit) {
      return new RecursionLimitException(message("macro_recursion_limit", limit, GUIDE_BASE));
    }
  }

  private static final boolean DEBUG = Boolean.getBoolean("golo.debug.macros");
  private static final int RECURSION_LIMIT = Integer.getInteger("golo.macros.recursion-limit", 42);

  private GoloCompilationException.Builder exceptionBuilder;
  private final MacroFinder finder;

  private boolean expandRegularCalls = true;
  private boolean recurse = true;
  private int recursionLimit = RECURSION_LIMIT;
  private int recursionLevel = 0;
  private boolean defaultRecurse = true;

  public MacroExpansionIrVisitor(ClassLoader loader, boolean defaultRecurse, GoloCompilationException.Builder exceptionBuilder) {
    this.finder = new MacroFinder(loader);
    this.defaultRecurse = defaultRecurse;
    this.exceptionBuilder = exceptionBuilder;
  }

  private static void debug(String message, Object... args) {
    if (DEBUG || gololang.Runtime.debugMode()) {
      info("Macro expansion: " + String.format(message, args));
    }
  }

  /**
   * Reset the internal state for the given module.
   *
   * We don't keep the previous state. Should we ever implement submodules, the previous state would then need to be
   * restored when leaving the submodule (state stack).
   */
  private MacroExpansionIrVisitor reset(GoloModule module) {
    this.finder.init(module.getImports().stream().map(mi -> mi.getPackageAndClass().toString()));
    this.expandRegularCalls = true;
    this.recurse = defaultRecurse;
    this.recursionLimit = RECURSION_LIMIT;
    this.recursionLevel = 0;
    if (this.exceptionBuilder == null) {
      this.exceptionBuilder = new GoloCompilationException.Builder(module == null ? "null" : module.sourceFile());
    }
    debug("reset for module %s", module);
    return this;
  }

  /**
   * Defines if the macros must be expanded recursively.
   * <p>
   * Mainly for debugging purpose.
   */
  public MacroExpansionIrVisitor recurse(boolean v) {
    this.recurse = v;
    return this;
  }

  /**
   * Check if we must recurse.
   */
  private boolean mustRecurse(AbstractInvocation<?> macro) {
    if (recursionLimit > 0 && recursionLevel >= recursionLimit) {
      expansionFailed(macro, RecursionLimitException.of(recursionLimit));
      return false;
    }
    return this.recurse;
  }

  /**
   * Defines the expansion recursion limit.
   */
  public MacroExpansionIrVisitor recursionLimit(int v) {
    this.recursionLimit = v;
    return this;
  }

  /**
   * Returns the current macro recursion level.
   */
  public int recursionLevel() {
    return this.recursionLevel;
  }

  /**
   * Defines if regular function invocations must be tried to expand.
   * <p>
   * Mainly for debugging purpose.
   */
  public MacroExpansionIrVisitor expandRegularCalls(boolean v) {
    this.expandRegularCalls = v;
    return this;
  }

  public void setExceptionBuilder(GoloCompilationException.Builder builder) {
    exceptionBuilder = builder;
  }

  private void replace(AbstractInvocation<?> invocation, GoloElement<?> original, GoloElement<?> replacement) {
    try {
      original.replaceInParentBy(replacement);
    } catch (StopCompilationException t) {
      // TODO: test
        throw t;
    } catch (Throwable t) {
      expansionFailed(invocation, t);
    }
  }

  @Override
  public void visitFunction(GoloFunction function) {
    GoloElement<?> converted = convertMacroDecorator(function);
    if (converted instanceof MacroInvocation) {
      replace((MacroInvocation) converted, function, converted);
      converted.accept(this);
    } else {
      function.walk(this);
    }
  }

  /**
   * Convert a function with macro decorators into nested macro calls.
   * <p>
   * The function node is <em>mutated</em> (decorator removed).
   */
  private GoloElement<?> convertMacroDecorator(GoloFunction function) {
    GoloFunction newFunction = GoloFunction.function(function);
    GoloElement<?> newElement = newFunction;
    List<Decorator> decos = new LinkedList<>();
    for (Decorator decorator : function.getDecorators()) {
      MacroInvocation invocation = decoratorToMacroInvocation(decorator, newElement);
      if (invocation != null && macroExists(invocation)) {
        newElement = invocation;
      } else {
        decos.add(decorator);
      }
    }
    if (newElement != newFunction) {
      newFunction.block(function.getBlock());
      for (Decorator d : decos) {
        newFunction.addDecorator(d);
      }
      return newElement;
    }
    return function;
  }

  /**
   * Convert a macro decorator into macro call on the function declaration.
   * <p>
   * For instance
   * <pre><code>
   * @myMacro
   * function foo = |x| -> x
   * </code></pre>
   *
   * is converted into something equivalent to:
   * <pre><code>
   * &myMacro {
   * function foo = |x| -> x
   * }
   * </pre></code>
   * that is a macro call on a function declaration node.
   */
  private MacroInvocation decoratorToMacroInvocation(Decorator decorator, GoloElement<?> function) {
    ExpressionStatement<?> expr = decorator.expression();
    if (expr instanceof FunctionInvocation) {
      FunctionInvocation invocation = (FunctionInvocation) expr;
      return MacroInvocation.call(invocation.getName())
        .withArgs(invocation.getArguments().toArray())
        .withArgs(function);
    } else if (expr instanceof ReferenceLookup) {
      return MacroInvocation.call(((ReferenceLookup) expr).getName())
        .withArgs(function);
    } else if (expr instanceof ClosureReference) {
      // Not (yet?) a valid macro call
      return null;
    } else if (expr instanceof BinaryOperation) {
      // Not (yet?) a valid macro call
      return null;
    } else {
      // must not happen
      throw new IllegalArgumentException("Invalid decorator type");
    }
  }

  public GoloElement<?> expand(GoloElement<?> element) {
    element.accept(this);
    return element;
  }

  @Override
  public void visitModule(GoloModule module) {
    this.reset(module);
    module.walk(this);
    module.decoratorMacro().map(this::expandMacro);
    module.decoratorMacro(null);
  }

  @Override
  public void visitMacroInvocation(MacroInvocation macroInvocation) {
    macroInvocation.walk(this);
    GoloElement<?> expanded = expandMacro(macroInvocation);
    replace(macroInvocation, macroInvocation, expanded);
    if (mustRecurse(macroInvocation)) {
      recursionLevel++;
      expanded.accept(this);
      recursionLevel--;
    }
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation macroInvocation) {
    macroInvocation.walk(this);
    if (tryExpand(macroInvocation)) {
      // Let's try to expand a regular call as a macro
      GoloElement<?> expanded = expandMacro(macroInvocation);
      if (expanded == null) {
        // Maybe it was not a macro after all...
        return;
      }
      replace(macroInvocation, macroInvocation, expanded);
      if (mustRecurse(macroInvocation)) {
        recursionLevel++;
        expanded.accept(this);
        recursionLevel--;
      }
    }
  }

  private boolean tryExpand(FunctionInvocation invocation) {
    return expandRegularCalls && !invocation.isAnonymous() && !invocation.isConstant();
  }

  public MacroExpansionIrVisitor useMacroModule(String name) {
    this.finder.addMacroClass(name);
    return this;
  }

  private GoloElement<?> expandMacro(FunctionInvocation invocation) {
    debug("try to expand %s", invocation);
    Optional<MethodHandle> macro = findMacro(invocation);
    if (!macro.isPresent()) {
      debug("macro not found");
      return null;
    }
    return macro.map(invokeMacroWith(invocation)).orElse(noMacroResult(invocation.getName()));
  }

  private GoloElement<?> expandMacro(MacroInvocation invocation) {
    debug("try to expand %s", invocation);
    return findMacro(invocation)
      .map(invokeMacroWith(invocation))
      .orElse(noMacroResult(invocation.getName()));
  }

  private Function<MethodHandle, GoloElement<?>> invokeMacroWith(AbstractInvocation<?> invocation) {
    return (macro) -> {
      try {
        GoloElement<?> result = (GoloElement<?>) macro.invokeWithArguments(invocation.getArguments());
        debug("macro expanded to %s", result);
        return result;
      } catch (StopCompilationException e) {
        throw e;
      } catch (Throwable t) {
        expansionFailed(invocation, t);
        debug("expansion failed");
        return null;
      }
    };
  }

  private GoloElement<?> noMacroResult(String macroName) {
    return Noop.of("macro `" + macroName + "` expanded without results");
  }

  private void loadingFailed(MacroInvocation invocation) {
    String errorMessage = message("macro_loading_failed", invocation.getName(), invocation.getArity())
        + ' ' + position(invocation) + ".";
    exceptionBuilder.report(UNKNOWN_MACRO, invocation, errorMessage);
  }

  private void expansionFailed(AbstractInvocation<?> invocation, Throwable t) {
    String errorMessage = message("macro_expansion_failed", invocation.getName())
      + ' ' + position(invocation) + ".";
    exceptionBuilder.report(MACRO_EXPANSION, invocation, errorMessage, t);
  }

  private String position(GoloElement<?> elt) {
    PositionInSourceCode position = elt.positionInSourceCode();
    if (position == null || position.isUndefined()) {
      return message("generated_code");
    }
    return message("source_position", position.getStartLine(), position.getStartColumn());
  }

  private Optional<MethodHandle> findMacro(FunctionInvocation invocation) {
    return finder.find(invocation).map(m -> m.binded(this, invocation));
  }

  private Optional<MethodHandle> findMacro(MacroInvocation invocation) {
    Optional<MethodHandle> macro = finder.find(invocation).map(m -> m.binded(this, invocation));
    if (!macro.isPresent()) {
      loadingFailed(invocation);
    }
    return macro;
  }

  public boolean macroExists(MacroInvocation invocation) {
    requireNonNull(invocation);
    boolean exists = finder.find(invocation).isPresent();
    debug("Check if %s exists: %s", invocation.getName(), exists);
    return exists;
  }
}
