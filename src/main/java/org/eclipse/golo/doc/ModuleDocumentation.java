/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.doc;

import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.PackageAndClass;
import gololang.ir.*;

import java.util.*;
import java.io.File;

public class ModuleDocumentation implements DocumentationElement {

  private String sourceFile;
  private PackageAndClass moduleName;
  private int moduleDefLine;
  private String moduleDocumentation;

  private final Map<String, Integer> imports = new TreeMap<>();
  private final Map<String, Integer> moduleStates = new TreeMap<>();
  private final SortedSet<FunctionDocumentation> functions = new TreeSet<>();
  private final SortedSet<FunctionDocumentation> macros = new TreeSet<>();
  private final Map<String, AugmentationDocumentation> augmentations = new TreeMap<>();
  private final SortedSet<StructDocumentation> structs = new TreeSet<>();
  private final SortedSet<UnionDocumentation> unions = new TreeSet<>();
  private final Set<NamedAugmentationDocumentation> namedAugmentations = new TreeSet<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String type() {
    return "module";
  }

  ModuleDocumentation(GoloModule module) {
    module.accept(new ModuleVisitor());
  }

  ModuleDocumentation() {

  }

  public static ModuleDocumentation load(File file, GoloCompiler compiler) throws java.io.IOException {
    return new ModuleDocumentation(compiler.expand(compiler.transform(compiler.parse(file))));
  }

  public static ModuleDocumentation empty(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Can't create empty module documentation without name");
    }
    ModuleDocumentation doc = new ModuleDocumentation();
    doc.moduleName = PackageAndClass.of(name);
    return doc;
  }

  public boolean isEmpty() {
    return moduleStates.isEmpty()
      && functions.isEmpty()
      && macros.isEmpty()
      && augmentations.isEmpty()
      && structs.isEmpty()
      && unions.isEmpty()
      && namedAugmentations.isEmpty();
  }

  public String goloVersion() {
    return org.eclipse.golo.cli.command.Metadata.VERSION;
  }

  public SortedSet<StructDocumentation> structs() {
    return structs;
  }

  public SortedSet<UnionDocumentation> unions() {
    return unions;
  }

  public SortedSet<FunctionDocumentation> functions() {
    return functions(false);
  }

  public SortedSet<FunctionDocumentation> functions(boolean withLocal) {
    if (withLocal) {
      return functions;
    }
    TreeSet<FunctionDocumentation> pubFunctions = new TreeSet<>();
    for (FunctionDocumentation f : functions) {
      if (!f.local()) {
        pubFunctions.add(f);
      }
    }
    return pubFunctions;
  }

  public String sourceFile() {
    return this.sourceFile;
  }

  public SortedSet<FunctionDocumentation> macros() {
    return macros;
  }

  public String moduleName() {
    return moduleName.toString();
  }

  public String packageName() {
    return moduleName.packageName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String label() {
    return moduleName.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return moduleName.className();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String fullName() {
    return moduleName.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String id() {
    return "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DocumentationElement parent() {
    return this;
  }

  public int moduleDefLine() {
    return moduleDefLine;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int line() {
    return moduleDefLine;
  }

  public String moduleDocumentation() {
    return (moduleDocumentation != null) ? moduleDocumentation : "\n";
  }

  public ModuleDocumentation moduleDocumentation(String doc) {
    this.moduleDocumentation = doc;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String documentation() {
    return moduleDocumentation();
  }

  public Map<String, Integer> moduleStates() {
    return moduleStates;
  }

  public Collection<AugmentationDocumentation> augmentations() {
    return augmentations.values();
  }

  public Collection<NamedAugmentationDocumentation> namedAugmentations() {
    return namedAugmentations;
  }

  public Map<String, Integer> imports() {
    return imports;
  }

  private class ModuleVisitor implements GoloIrVisitor {

    private final Deque<Set<FunctionDocumentation>> functionContext = new LinkedList<>();
    private UnionDocumentation currentUnion;
    private MemberHolder currentMemberHolder;
    private final Deque<DocumentationElement> parents = new LinkedList<>();

    @Override
    public void visitModule(GoloModule module) {
      functionContext.push(functions);
      parents.push(ModuleDocumentation.this);
      moduleName = module.getPackageAndClass();
      moduleDefLine = module.positionInSourceCode().getStartLine();
      moduleDocumentation = module.documentation();
      sourceFile = module.sourceFile();
      module.walk(this);
    }

    @Override
    public void visitModuleImport(ModuleImport moduleImport) {
      if (!moduleImport.isImplicit()) {
        imports.put(moduleImport.getPackageAndClass().toString(), moduleImport.positionInSourceCode().getStartLine());
      }
    }

    @Override
    public void visitStruct(Struct struct) {
      StructDocumentation doc = new StructDocumentation()
              .parent(parents.peek())
              .name(struct.getName())
              .documentation(struct.documentation())
              .line(struct.positionInSourceCode().getStartLine());
      structs.add(doc);
      currentMemberHolder = doc;
      parents.push(doc);
      struct.walk(this);
      parents.pop();
      currentMemberHolder = null;
    }

    @Override
    public void visitUnion(Union union) {
      this.currentUnion = new UnionDocumentation()
          .parent(parents.peek())
          .name(union.getName())
          .documentation(union.documentation())
          .line(union.positionInSourceCode().getStartLine());
      unions.add(this.currentUnion);
      parents.push(currentUnion);
      union.walk(this);
      parents.pop();
    }

    @Override
    public void visitUnionValue(UnionValue value) {
      UnionDocumentation.UnionValueDocumentation doc = this.currentUnion.addValue(value.getName())
          .parent(parents.peek())
          .documentation(value.documentation())
          .line(value.positionInSourceCode().getStartLine());
      currentMemberHolder = doc;
      parents.push(doc);
      value.walk(this);
      parents.pop();
      currentMemberHolder = null;
    }

    @Override
    public void visitAugmentation(Augmentation augment) {
      String target = augment.getTarget().toString();
      if (!augmentations.containsKey(target)) {
        augmentations.put(target, new AugmentationDocumentation()
                .target(target)
                .parent(parents.peek())
                .augmentationNames(augment.getNames())
                .line(augment.positionInSourceCode().getStartLine())
        );
      }
      AugmentationDocumentation ad = augmentations.get(target);
      if (augment.documentation() != null && !augment.documentation().isEmpty()) {
        ad.documentation(String.join("\n", ad.documentation(), augment.documentation()));
      }
      functionContext.push(ad);
      parents.push(ad);
      augment.walk(this);
      functionContext.pop();
      parents.pop();
    }

    @Override
    public void visitNamedAugmentation(NamedAugmentation augment) {
      NamedAugmentationDocumentation augmentDoc = new NamedAugmentationDocumentation()
          .parent(parents.peek())
          .name(augment.getName())
          .documentation(augment.documentation())
          .line(augment.positionInSourceCode().getStartLine());
      namedAugmentations.add(augmentDoc);
      functionContext.push(augmentDoc);
      parents.push(augmentDoc);
      augment.walk(this);
      functionContext.pop();
      parents.pop();
    }

    @Override
    public void visitFunction(GoloFunction function) {
      if (!GoloModule.MODULE_INITIALIZER_FUNCTION.equals(function.getName())) {
        FunctionDocumentation fdoc = new FunctionDocumentation(function.isMacro())
          .parent(parents.peek())
          .name(function.getName())
          .documentation(function.documentation())
          .augmentation(function.isInAugment())
          .line(function.positionInSourceCode().getStartLine())
          .local(function.isLocal())
          .arguments(function.getParameterNames())
          .varargs(function.isVarargs());
        (function.isMacro() ? macros : functionContext.peek()).add(fdoc);
      }
    }

    @Override
    public void visitLocalReference(LocalReference localRef) {
      if (localRef.isModuleState()) {
        moduleStates.put(localRef.getName(), localRef.positionInSourceCode().getStartLine());
      }
    }

    @Override
    public void visitMember(Member member) {
      currentMemberHolder.addMember(member.getName())
        .parent(parents.peek())
        .documentation(member.documentation())
        .line(member.positionInSourceCode().getStartLine());
    }

    @Override
    public void visitDecorator(Decorator decorator) {
    }

    @Override
    public void visitBlock(Block block) {
    }

    @Override
    public void visitConstantStatement(ConstantStatement constantStatement) {
    }

    @Override
    public void visitReturnStatement(ReturnStatement returnStatement) {
    }

    @Override
    public void visitFunctionInvocation(FunctionInvocation functionInvocation) {
    }

    @Override
    public void visitMethodInvocation(MethodInvocation methodInvocation) {
    }

    @Override
    public void visitAssignmentStatement(AssignmentStatement assignmentStatement) {
    }

    @Override
    public void visitDestructuringAssignment(DestructuringAssignment assignment) {
    }

    @Override
    public void visitReferenceLookup(ReferenceLookup referenceLookup) {
    }

    @Override
    public void visitConditionalBranching(ConditionalBranching conditionalBranching) {
    }

    @Override
    public void visitBinaryOperation(BinaryOperation binaryOperation) {
    }

    @Override
    public void visitUnaryOperation(UnaryOperation unaryOperation) {
    }

    @Override
    public void visitLoopStatement(LoopStatement loopStatement) {
    }

    @Override
    public void visitForEachLoopStatement(ForEachLoopStatement foreachStatement) {
    }

    @Override
    public void visitCaseStatement(CaseStatement caseStatement) {
    }

    @Override
    public void visitMatchExpression(MatchExpression matchExpression) {
    }

    @Override
    public void visitWhenClause(WhenClause<?> whenClause) {
    }

    @Override
    public void visitThrowStatement(ThrowStatement throwStatement) {
    }

    @Override
    public void visitTryCatchFinally(TryCatchFinally tryCatchFinally) {
    }

    @Override
    public void visitClosureReference(ClosureReference closureReference) {
    }

    @Override
    public void visitLoopBreakFlowStatement(LoopBreakFlowStatement loopBreakFlowStatement) {
    }

    @Override
    public void visitCollectionLiteral(CollectionLiteral collectionLiteral) {
    }

    @Override
    public void visitCollectionComprehension(CollectionComprehension collectionComprehension) {
    }

    @Override
    public void visitNamedArgument(NamedArgument namedArgument) {
    }

    @Override
    public void visitMacroInvocation(MacroInvocation call) {
    }

    @Override
    public void visitNoop(Noop noop) {
    }

    @Override
    public void visitToplevelElements(ToplevelElements toplevels) {
      toplevels.walk(this);
    }
  }
}
