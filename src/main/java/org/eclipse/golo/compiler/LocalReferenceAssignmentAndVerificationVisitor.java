/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import gololang.ir.*;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.eclipse.golo.compiler.GoloCompilationException.Problem.Type.*;
import static gololang.Messages.message;
import static gololang.Messages.prefixed;

public class LocalReferenceAssignmentAndVerificationVisitor extends AbstractGoloIrVisitor {

  private GoloModule module = null;
  private final AssignmentCounter assignmentCounter = new AssignmentCounter();
  private final Deque<GoloFunction> functionStack = new LinkedList<>();
  private final Deque<ReferenceTable> tableStack = new LinkedList<>();
  private final Deque<Set<LocalReference>> assignmentStack = new LinkedList<>();
  private GoloCompilationException.Builder exceptionBuilder;
  private final HashSet<LocalReference> uninitializedReferences = new HashSet<>();


  private static class AssignmentCounter {

    private int counter = 0;

    public int next() {
      return counter++;
    }

    public void reset() {
      counter = 0;
    }
  }

  LocalReferenceAssignmentAndVerificationVisitor() { }

  LocalReferenceAssignmentAndVerificationVisitor(GoloCompilationException.Builder builder) {
    this();
    setExceptionBuilder(builder);
  }

  public void setExceptionBuilder(GoloCompilationException.Builder builder) {
    exceptionBuilder = builder;
  }

  private GoloCompilationException.Builder getExceptionBuilder() {
    if (exceptionBuilder == null) {
      exceptionBuilder = new GoloCompilationException.Builder(module.getPackageAndClass().toString());
    }
    return exceptionBuilder;
  }

  private void errorMessage(GoloCompilationException.Problem.Type type, GoloElement<?> node, String message) {
    PositionInSourceCode position = null;
    if (node != null) {
      position = node.positionInSourceCode();
    }
    String errorMessage = message + ' ' + (
        (position != null || position.isUndefined())
        ? message("source_position", position.getStartLine(), position.getStartColumn())
        : message("generated_code")) + ".";

    getExceptionBuilder().report(type, node, errorMessage);
  }

  @Override
  public void visitModule(GoloModule module) {
    this.module = module;
    module.walk(this);
  }

  @Override
  public void visitFunction(GoloFunction function) {
    assignmentCounter.reset();
    functionStack.push(function);
    ReferenceTable table = function.getBlock().getReferenceTable();
    for (String parameterName : function.getParameterNames()) {
      LocalReference reference = table.get(parameterName);
      uninitializedReferences.remove(reference);
      if (reference == null) {
        if (!function.isSynthetic()) {
          throw new IllegalStateException(
              prefixed("bug", message("parameter_not_declared", parameterName, function.getName())));
        }
      } else {
        reference.setIndex(assignmentCounter.next());
      }
    }
    function.walk(this);
    functionStack.pop();
  }

  @Override
  public void visitBlock(Block block) {
    ReferenceTable table = block.getReferenceTable();
    extractUninitializedReferences(table);
    tableStack.push(table);
    assignmentStack.push(extractAssignedReferences(table));
    block.walk(this);
    assignmentStack.pop();
    tableStack.pop();
  }

  private void extractUninitializedReferences(ReferenceTable table) {
    for (LocalReference reference : table.ownedReferences()) {
      if (reference.getIndex() < 0 && !reference.isModuleState()) {
        reference.setIndex(assignmentCounter.next());
        uninitializedReferences.add(reference);
      }
    }
  }

  private Set<LocalReference> extractAssignedReferences(ReferenceTable table) {
    HashSet<LocalReference> assigned = new HashSet<>();
    if (table == functionStack.peek().getBlock().getReferenceTable()) {
      for (String param : functionStack.peek().getParameterNames()) {
        assigned.add(table.get(param));
      }
    }
    if (!assignmentStack.isEmpty()) {
      assigned.addAll(assignmentStack.peek());
    }
    return assigned;
  }

  @Override
  public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    if (!tableStack.isEmpty()) {
      if (tableStack.peek().hasReferenceFor(functionInvocation.getName())) {
        if (tableStack.peek().get(functionInvocation.getName()).isModuleState()) {
          functionInvocation.onModuleState();
        } else {
          functionInvocation.onReference();
        }
      }
    }
    functionInvocation.walk(this);
  }

  @Override
  public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    LocalReference reference = assignmentStatement.getLocalReference();
    Set<LocalReference> assignedReferences = assignmentStack.peek();
    if (redeclaringReferenceInBlock(assignmentStatement, reference, assignedReferences)) {
      errorMessage(REFERENCE_ALREADY_DECLARED_IN_BLOCK, assignmentStatement,
          message("reference_already_declared", reference.getName()));
    } else if (assigningConstant(reference, assignedReferences)) {
      errorMessage(ASSIGN_CONSTANT, assignmentStatement,
          message("assign_constant", reference.getName()));
    }
    bindReference(reference);
    assignedReferences.add(reference);
    assignmentStatement.walk(this);
    if (assignmentStatement.isDeclaring() && !reference.isSynthetic()) {
      uninitializedReferences.remove(reference);
    }
  }

  private void bindReference(LocalReference reference) {
    ReferenceTable table = tableStack.peek();
    if (reference.getIndex() < 0) {
      if (table.hasReferenceFor(reference.getName())) {
        reference.setIndex(table.get(reference.getName()).getIndex());
      } else if (reference.isSynthetic()) {
        reference.setIndex(assignmentCounter.next());
        table.add(reference);
      }
    }
  }

  private boolean redeclaringReferenceInBlock(AssignmentStatement assignmentStatement, LocalReference reference, Set<LocalReference> assignedReferences) {
    return !reference.isSynthetic() && assignmentStatement.isDeclaring() && referenceNameExists(reference, assignedReferences);
  }

  private boolean assigningConstant(LocalReference reference, Set<LocalReference> assignedReferences) {
    return reference.isConstant() && (
        assignedReferences.contains(reference)
        || (reference.isModuleState() && !functionStack.peek().isModuleInit()));
  }

  private boolean referenceNameExists(LocalReference reference, Set<LocalReference> referencesInBlock) {
    for (LocalReference ref : referencesInBlock) {
      if ((ref != null) && ref.getName().equals(reference.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    ReferenceTable table = tableStack.peek();
    if (table == null) { return; }
    if (!table.hasReferenceFor(referenceLookup.getName())) {
      errorMessage(UNDECLARED_REFERENCE, referenceLookup,
          message("undeclared_reference", referenceLookup.getName(),
          !functionStack.isEmpty()
            ? message("in_function", functionStack.peek().getName())
            : ""));
    }
    LocalReference ref = referenceLookup.resolveIn(table);
    if (isUninitialized(ref)) {
      errorMessage(UNINITIALIZED_REFERENCE_ACCESS, referenceLookup,
          message("uninitialized_reference_access", ref.getName()));
    }
  }

  private boolean isUninitialized(LocalReference ref) {
    return ref != null && !ref.isSynthetic() && !ref.isModuleState() && uninitializedReferences.contains(ref);
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) { }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    if (loopBreakFlowStatement.getEnclosingLoop() == null) {
      errorMessage(BREAK_OR_CONTINUE_OUTSIDE_LOOP, loopBreakFlowStatement,
          message("break_or_continue_outside_loop"));
    }
  }

  @Override
  public void visitMember(Member member) { }
}
