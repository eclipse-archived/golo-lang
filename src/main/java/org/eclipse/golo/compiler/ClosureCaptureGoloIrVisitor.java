/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler;

import org.eclipse.golo.compiler.ir.*;

import java.util.*;

public class ClosureCaptureGoloIrVisitor extends AbstractGoloIrVisitor {

  private static final class Context {
    final Set<String> parameterReferences = new HashSet<>();
    final Set<String> allReferences = new HashSet<>();
    final Set<String> localReferences = new HashSet<>();
    final Set<String> accessedReferences = new HashSet<>();
    final Map<String, Block> definingBlock = new HashMap<>();
    final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();

    Set<String> shouldBeParameters() {
      Set<String> result = new HashSet<>();
      for (String refName : accessedReferences) {
        if (!localReferences.contains(refName)) {
          result.add(refName);
        }
      }
      return result;
    }

    Set<String> shouldBeRemoved() {
      Set<String> result = new HashSet<>(allReferences);
      for (String ref : accessedReferences) {
        result.remove(ref);
      }
      return result;
    }
  }

  private final Deque<Context> stack = new LinkedList<>();

  private Context context() {
    return stack.peek();
  }

  private void newContext() {
    stack.push(new Context());
  }

  private void dropContext() {
    stack.pop();
  }

  private void dropBlockTable() {
    if (!stack.isEmpty()) {
      context().referenceTableStack.pop();
    }
  }

  private void pushBlockTable(Block block) {
    if (!stack.isEmpty()) {
      if (!context().referenceTableStack.isEmpty()) {
        block.getReferenceTable().relink(context().referenceTableStack.peek());
      }
      context().referenceTableStack.push(block.getReferenceTable());
    }
  }

  private void locallyDeclared(String name) {
    if (!stack.isEmpty()) {
      context().localReferences.add(name);
    }
  }

  private void locallyAssigned(String name) {
    if (!stack.isEmpty()) {
      context().accessedReferences.add(name);
    }
  }

  private void accessed(String name) {
    if (!stack.isEmpty()) {
      context().accessedReferences.add(name);
    }
  }

  private void definedInBlock(Set<String> references, Block block) {
    if (!stack.isEmpty()) {
      for (String ref : references) {
        context().definingBlock.put(ref, block);
      }
      context().allReferences.addAll(references);
    }
  }

  private void declaredParameters(List<String> references) {
    context().parameterReferences.addAll(references);
  }

  @Override
  public void visitFunction(GoloFunction function) {
    if (function.isSynthetic()) {
      newContext();
      declaredParameters(function.getParameterNames());
      function.getBlock().internReferenceTable();
      function.walk(this);
      function.addSyntheticParameters(context().shouldBeParameters());
      dropUnused(context().shouldBeRemoved());
      dropContext();
    } else {
      function.walk(this);
    }
    function.captureClosedReference();
  }

  private void dropUnused(Set<String> refs) {
    Context context = context();
    for (String ref : refs) {
      if (!context.parameterReferences.contains(ref)) {
        context.definingBlock.get(ref).getReferenceTable().remove(ref);
      }
    }
  }

  @Override
  public void visitBlock(Block block) {
    pushBlockTable(block);
    definedInBlock(block.getReferenceTable().ownedSymbols(), block);
    block.walk(this);
    dropBlockTable();
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    if (context() != null) {
      Context context = context();
      String name = functionInvocation.getName();
      if (context.allReferences.contains(name)) {
        accessed(name);
      }
    }
    functionInvocation.walk(this);
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    LocalReference localReference = assignmentStatement.getLocalReference();
    String referenceName = localReference.getName();
    if (!localReference.isModuleState()) {
      if (!stack.isEmpty()) {
        assignmentStatement.to(context().referenceTableStack.peek().get(referenceName));
      }
      if (assignmentStatement.isDeclaring()) {
        locallyDeclared(referenceName);
      }
    } else {
      locallyDeclared(referenceName);
    }
    locallyAssigned(referenceName);
    assignmentStatement.walk(this);
    if (assignmentStatement.getExpressionStatement() instanceof ClosureReference) {
      ClosureReference closure = (ClosureReference) assignmentStatement.getExpressionStatement();
      closure.getTarget().setSyntheticSelfName(referenceName);
    }
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    accessed(referenceLookup.getName());
  }

  @Override
  public void visitThrowStatement(ThrowStatement throwStatement) {
    throwStatement.getExpressionStatement().accept(this);
  }

  @Override
  public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    tryCatchFinally.getTryBlock().accept(this);
    if (tryCatchFinally.hasCatchBlock()) {
      locallyAssigned(tryCatchFinally.getExceptionId());
      locallyDeclared(tryCatchFinally.getExceptionId());
      tryCatchFinally.getCatchBlock().accept(this);
    }
    if (tryCatchFinally.hasFinallyBlock()) {
      tryCatchFinally.getFinallyBlock().accept(this);
    }
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    closureReference.walk(this);
    if (closureReference.getTarget().isSynthetic()) {
      Context context = context();
      if (context != null) {
        for (String refName : closureReference.getTarget().getParameterNames()) {
          ReferenceTable referenceTable = context.referenceTableStack.peek();
          if (referenceTable.hasReferenceFor(refName)) {
            // ...else it's a regular parameter
            accessed(refName);
          }
        }
      }
    }
  }
}
