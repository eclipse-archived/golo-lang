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

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.eclipse.golo.compiler.GoloCompilationException.Problem.Type.*;

class LocalReferenceAssignmentAndVerificationVisitor extends AbstractGoloIrVisitor {

  private GoloModule module = null;
  private AssignmentCounter assignmentCounter = new AssignmentCounter();
  private Deque<GoloFunction> functionStack = new LinkedList<>();
  private Deque<ReferenceTable> tableStack = new LinkedList<>();
  private Deque<Set<LocalReference>> assignmentStack = new LinkedList<>();
  private Deque<LoopStatement> loopStack = new LinkedList<>();
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
          throw new IllegalStateException("[please report this bug] "
              + parameterName + " is not declared in the references of function "
              + function.getName());
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
    if (assigningConstant(reference, assignedReferences)) {
      getExceptionBuilder().report(ASSIGN_CONSTANT, assignmentStatement.getASTNode(),
          "Assigning `" + reference.getName() +
              "` at " + assignmentStatement.getPositionInSourceCode() +
              " but it is a constant reference"
      );
    } else if (redeclaringReferenceInBlock(assignmentStatement, reference, assignedReferences)) {
      getExceptionBuilder().report(REFERENCE_ALREADY_DECLARED_IN_BLOCK, assignmentStatement.getASTNode(),
          "Declaring a duplicate reference `" + reference.getName() +
              "` at " + assignmentStatement.getPositionInSourceCode()
      );
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
        assignedReferences.contains(reference) ||
        (reference.isModuleState() && !functionStack.peek().isModuleInit()));
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
      getExceptionBuilder().report(UNDECLARED_REFERENCE, referenceLookup.getASTNode(),
          "Undeclared reference `" + referenceLookup.getName() + "`"
          + (!functionStack.isEmpty() ? " in "
            + (functionStack.peek().isSynthetic() ? "synthetic " : "")
            + "function `" + functionStack.peek().getName() + "`" : "")
          + (!referenceLookup.getPositionInSourceCode().isNull()
              ? " at " + referenceLookup.getPositionInSourceCode()
              : " (generated code)"));
    }
    LocalReference ref = referenceLookup.resolveIn(table);
    if (isUninitialized(ref)) {
      getExceptionBuilder().report(UNINITIALIZED_REFERENCE_ACCESS, referenceLookup.getASTNode(),
          "Uninitialized reference `" + ref.getName() + "` at " + referenceLookup.getPositionInSourceCode());
    }
  }

  private boolean isUninitialized(LocalReference ref) {
    return ref != null && !ref.isSynthetic() && !ref.isModuleState() && uninitializedReferences.contains(ref);
  }

  @Override
  public void visitLoopStatement(LoopStatement loopStatement) {
    loopStack.push(loopStatement);
    loopStatement.walk(this);
    loopStack.pop();
  }

  @Override
  public void visitClosureReference(ClosureReference closureReference) {
    closureReference.updateCapturedReferenceNames();
  }

  @Override
  public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    if (loopStack.isEmpty()) {
      getExceptionBuilder().report(BREAK_OR_CONTINUE_OUTSIDE_LOOP,
          loopBreakFlowStatement.getASTNode(),
          "continue or break statement outside a loop at " + loopBreakFlowStatement.getPositionInSourceCode());
    } else {
      loopBreakFlowStatement.setEnclosingLoop(loopStack.peek());
    }
  }

  @Override
  public void visitMember(Member member) {
    // We don't want to check references in member default values.
    // The check will be done in the generated factory that effectively uses the default value.
  }
}
