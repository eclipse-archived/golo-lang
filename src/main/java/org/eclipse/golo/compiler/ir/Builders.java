/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.runtime.OperatorType;
import org.eclipse.golo.compiler.parser.GoloParser;
import org.eclipse.golo.compiler.PackageAndClass;

public final class Builders {
  private Builders() {
    // utility static class
  }

  public static NamedArgument namedArgument(String name) {
    return new NamedArgument(name);
  }

  public static MethodInvocation invoke(String name) {
    return new MethodInvocation(name);
  }

  public static Augmentation augment(String target) {
    return new Augmentation(PackageAndClass.fromString(target));
  }

  public static Augmentation augment(PackageAndClass target) {
    return new Augmentation(target);
  }

  public static NamedAugmentation augmentation(String name) {
    return new NamedAugmentation(PackageAndClass.fromString(name));
  }

  public static NamedAugmentation augmentation(PackageAndClass name) {
    return new NamedAugmentation(name);
  }

  public static LoopStatement loop() {
    return new LoopStatement();
  }

  public static LoopStatement whileLoop(Object condition) {
    return loop().condition(condition);
  }

  public static ForEachLoopStatement foreach() {
    return new ForEachLoopStatement();
  }

  public static CaseStatement cases() {
    return new CaseStatement();
  }

  public static MatchExpression match() {
    return new MatchExpression();
  }

  public static AssignmentStatement assignment() {
    return new AssignmentStatement();
  }

  /**
   * Creates a assignment of the given expression.
   */
  public static AssignmentStatement assign(Object expression) {
    return assignment().as(expression);
  }

  /**
   * Creates a declaring assignment to a given reference.
   */
  public static AssignmentStatement define(Object reference) {
    return assignment().to(reference).declaring();
  }

  public static DestructuringAssignment destruct() {
    return new DestructuringAssignment();
  }

  public static UnaryOperation not(ExpressionStatement expression) {
    return new UnaryOperation(OperatorType.NOT, expression);
  }

  public static BinaryOperation binaryOperation(OperatorType type, Object left, Object right) {
    return binaryOperation(type).left(left).right(right);
  }

  public static BinaryOperation binaryOperation(Object type) {
    return BinaryOperation.of(type);
  }

  public static Decorator decorator(Object expr) {
    return new Decorator((ExpressionStatement) expr);
  }

  public static GoloFunction functionDeclaration() {
    return new GoloFunction();
  }

  public static GoloFunction functionDeclaration(String name) {
    return new GoloFunction().name(name);
  }

  public static ClosureReference lambda(String... parameters) {
    return functionDeclaration()
      .withParameters(parameters)
      .synthetic()
      .local()
      .asClosure()
      .asClosureReference();
  }

  public static FunctionInvocation functionInvocation() {
    return new FunctionInvocation();
  }

  public static FunctionInvocation call(String name) {
    return new FunctionInvocation(name);
  }

  public static BinaryOperation anonCall(Object receiver, Object invocation) {
    return binaryOperation(
        OperatorType.ANON_CALL,
        (ExpressionStatement) receiver,
        (FunctionInvocation) invocation);
  }

  public static Block block() {
    return Block.emptyBlock();
  }

  public static Block block(Object... statements) {
    Block block = Block.emptyBlock();
    for (Object st : statements) {
      block.add(st);
    }
    return block;
  }

  public static ReferenceLookup refLookup(String name) {
    return new ReferenceLookup(name);
  }

  public static ConstantStatement constant(Object value) {
    if (value instanceof Class) {
      return classRef(value);
    }
    if (value instanceof ConstantStatement) {
      return (ConstantStatement) value;
    }
    return new ConstantStatement(value);
  }

  public static ConstantStatement classRef(Object cls) {
    if (cls instanceof String) {
      return constant(toClassRef((String) cls));
    }
    if (cls instanceof Class) {
      return constant(toClassRef((Class) cls));
    }
    if (cls instanceof GoloParser.ParserClassRef) {
      return constant(cls);
    }
    if (cls instanceof PackageAndClass) {
      return constant(toClassRef(cls.toString()));
    }
    throw new IllegalArgumentException("unknown type " + cls.getClass() + "to build a class reference");
  }

  public static ConstantStatement functionRef(Object funcName) {
    return functionRef(null, funcName, -1);
  }

  public static ConstantStatement functionRef(Object moduleName, Object funcName) {
    return functionRef(moduleName, funcName, -1);
  }

  public static ConstantStatement functionRef(Object moduleName, Object funcName, Object arity) {
    return functionRef(moduleName, funcName, -1, false);
  }

  public static ConstantStatement functionRef(Object moduleName, Object funcName, Object arity, Object varargs) {
    return constant(new GoloParser.FunctionRef(
          (String) moduleName,
          (String) funcName,
          (Integer) arity,
          (Boolean) varargs));
  }

  public static ReturnStatement returns(Object expr) {
    return new ReturnStatement((ExpressionStatement) expr);
  }

  public static ThrowStatement raise(Object expression) {
    return new ThrowStatement((ExpressionStatement) expression);
  }

  public static LocalReference localRef(Object name) {
    return new LocalReference(name.toString());
  }

  public static GoloParser.ParserClassRef toClassRef(Class<?> cls) {
    return toClassRef(cls.getCanonicalName());
  }

  public static GoloParser.ParserClassRef toClassRef(String clsName) {
    return new GoloParser.ParserClassRef(clsName);
  }

  public static Struct structure(String name) {
    return new Struct(name);
  }

  public static Union union(String name) {
    return new Union(name);
  }

  public static CollectionLiteral collection(String type, Object... values) {
    return collection(CollectionLiteral.Type.valueOf(type), values);
  }

  public static CollectionLiteral collection(CollectionLiteral.Type type, Object... values) {
    CollectionLiteral col = new CollectionLiteral(type);
    for (Object v : values) {
      col.add(v);
    }
    return col;
  }

  public static CollectionLiteral list(Object... values) {
    return collection(CollectionLiteral.Type.list, values);
  }

  public static CollectionLiteral array(Object... values) {
    return collection(CollectionLiteral.Type.array, values);
  }

  public static CollectionLiteral set(Object... values) {
    return collection(CollectionLiteral.Type.set, values);
  }

  public static CollectionLiteral map(Object... values) {
    return collection(CollectionLiteral.Type.map, values);
  }

  public static CollectionLiteral tuple(Object... values) {
    return collection(CollectionLiteral.Type.tuple, values);
  }

  public static CollectionLiteral vector(Object... values) {
    return collection(CollectionLiteral.Type.vector, values);
  }

  public static CollectionLiteral range(Object... values) {
    return collection(CollectionLiteral.Type.range, values);
  }

  public static CollectionComprehension collectionComprehension(CollectionLiteral.Type type) {
    return new CollectionComprehension(type);
  }

  public static CollectionComprehension collectionComprehension(String typeName) {
    return collectionComprehension(CollectionLiteral.Type.valueOf(typeName));
  }

  public static CollectionComprehension arrayComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.array);
  }

  public static CollectionComprehension listComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.list);
  }

  public static CollectionComprehension setComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.set);
  }

  public static CollectionComprehension mapComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.map);
  }

  public static CollectionComprehension tupleComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.tuple);
  }

  public static CollectionComprehension vectorComprehension() {
    return new CollectionComprehension(CollectionLiteral.Type.vector);
  }

  public static TryCatchFinally tryCatch(String exceptionId) {
    return new TryCatchFinally(exceptionId);
  }

  public static GoloStatement toGoloStatement(Object statement) {
    if (statement == null) { return null; }
    if (statement instanceof GoloStatement) {
      return (GoloStatement) statement;
    }
    throw cantConvert(statement, "GoloStatement");
  }

  public static Block toBlock(Object block) {
    if (block == null) { return Block.emptyBlock(); }
    if (block instanceof Block) {
      return (Block) block;
    }
    throw cantConvert(block, "Block");
  }

  private static IllegalArgumentException cantConvert(Object value, String target) {
    return new IllegalArgumentException(
        String.format("%s is not a %s, but a %s",
        value, target, value.getClass()));
  }

  public static GoloModule module(PackageAndClass name) {
    return new GoloModule(name);
  }

  public static GoloModule module(String name) {
    return new GoloModule(PackageAndClass.fromString(name));
  }

  public static ModuleImport moduleImport(Object name) {
    if (name instanceof String) {
      return new ModuleImport(PackageAndClass.fromString((String) name));
    }
    if (name instanceof PackageAndClass) {
      return new ModuleImport((PackageAndClass) name);
    }
    throw cantConvert(name, "string or package");
  }

  public static ConditionalBranching branch() {
    return new ConditionalBranching();
  }

  public static ConditionalBranching branch(Object condition,
                                            Block trueBlock,
                                            Block falseBlock,
                                            ConditionalBranching elseBranch) {
    return branch().condition(condition)
      .whenTrue(trueBlock)
      .whenFalse(falseBlock)
      .elseBranch(elseBranch);
  }

  public static Member member(String name) {
    return new Member(name);
  }
}
