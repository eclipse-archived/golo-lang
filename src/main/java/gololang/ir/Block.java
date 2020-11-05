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

import java.util.*;
import gololang.FunctionReference;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represent a block, that is a sequence of statements.
 *
 * <p>A block defines a scope, and as such maintains its local reference table.
 */
public class Block extends ExpressionStatement<Block> implements Iterable<GoloStatement<?>> {

  private static final Block NULL = new Block() {
    @Override
    public void accept(GoloIrVisitor visitor) { }

    @Override
    public void merge(Block other) { throw new UnsupportedOperationException(); }

    @Override
    public Block ref(Object o) { return this; }

    @Override
    public void internReferenceTable() {}

    @Override
    public List<GoloStatement<?>> getStatements() { return Collections.emptyList(); }

    @Override
    public Iterator<GoloStatement<?>> iterator() { return Collections.emptyIterator(); }

    @Override
    public Block map(FunctionReference fun) throws Throwable { return this; }

    @Override
    public Block add(Object statement) { throw new UnsupportedOperationException(); }

    @Override
    public Block prepend(Object statement) { throw new UnsupportedOperationException(); }

    @Override
    public void flatten() {}

    @Override
    public int size() { return 0; }

    @Override
    public boolean hasOnlyReturn() { return false; }

    @Override
    public boolean hasOnlyExpression() { return false; }

    @Override
    public String toString() { return "{NULL}"; }

    @Override
    public boolean isEmpty() { return true; }

    @Override
    public void walk(GoloIrVisitor visitor) {}

    @Override
    public List<GoloElement<?>> children() { return Collections.emptyList(); }

    @Override
    protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
      throw new UnsupportedOperationException();
    }
  };

  private final List<GoloStatement<?>> statements;
  private ReferenceTable referenceTable;
  private boolean hasReturn = false;

  private Block() {
    super();
    statements = Collections.emptyList();
  }

  Block(ReferenceTable referenceTable) {
    super();
    this.referenceTable = requireNonNull(referenceTable);
    this.statements = new LinkedList<>();
  }

  protected Block self() { return this; }

  /**
   * Create an empty block.
   */
  public static Block empty() {
    return new Block(new ReferenceTable());
  }

  /**
   * Return the null block.
   *
   * Null Object that does nothing.
   */
  public static Block nullBlock() {
    return NULL;
  }

  /**
   * Block coercion.
   *
   * <p> if the argument is {@code null}, returns an empty block, otherwise, tries to convert it into a block, wrapping
   * it in an empty block if necessary.
   */
  public static Block of(Object block) {
    if (block == null) {
      return empty();
    }
    if (block instanceof Block) {
      return (Block) block;
    }
    if (block instanceof GoloStatement<?>) {
      return empty().add(block);
    }
    throw cantConvert("Block", block);
  }

  /**
   * Creates a block containing the given statements.
   *
   * <p>If the only argument is already a block, returns it unchanged. If no arguments are given, returns an empty block.
   *
   * @param statements the {@link GoloStatement}s composing the block.
   */
  public static Block block(Object... statements) {
    if (statements.length == 1) {
      return of(statements[0]);
    }
    Block block = empty();
    for (Object st : statements) {
      block.add(st);
    }
    return block;
  }

  /**
   * Merge two blocks.
   *
   * <p><strong>Warning:</strong>the statements contained in the other block being <em>move</em> to this one and <em>not copied</em>,
   * the other block references statements but is no more their parent. Therefore, the other block is not
   * in a consistent state and should be dropped, since no more needed.
   */
  public void merge(Block other) {
    for (GoloStatement<?> innerStatement : other.getStatements()) {
      this.add(innerStatement);
    }
  }

  public ReferenceTable getReferenceTable() {
    return referenceTable;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ReferenceTable> getLocalReferenceTable() {
    return Optional.ofNullable(referenceTable);
  }

  /**
   * Define the reference table of this block.
   *
   * <p>This is a builder method.
   *
   * @param referenceTable the {@link ReferenceTable} to use in this block.
   * @return this block
   */
  public Block ref(Object referenceTable) {
    if (referenceTable instanceof ReferenceTable) {
      this.referenceTable = (ReferenceTable) referenceTable;
      return this;
    }
    throw new IllegalArgumentException("not a reference table");
  }

  /**
   * Internal method.
   */
  public void internReferenceTable() {
    this.referenceTable = referenceTable.flatDeepCopy(true);
  }

  public List<GoloStatement<?>> getStatements() {
    return unmodifiableList(statements);
  }

  public Iterator<GoloStatement<?>> iterator() {
    return statements.iterator();
  }

  /**
   * Creates a new block by applying the given function to the elements of this block.
   */
  public Block map(FunctionReference fun) throws Throwable {
    Block res = empty();
    for (GoloElement<?> elt : this) {
      res.add(fun.invoke(elt));
    }
    return res;
  }

  /**
   * Add a statement to this block.
   *
   * <p>This is a builder method.
   *
   * @param statement the statement to add to this block
   * @return this block
   * @throws ClassCastException if the statement can't be converted into a {@link GoloStatement}
   * @see GoloStatement#of(Object)
   */
  public Block add(Object statement) {
    if (statement != null) {
      this.addStatement(statements.size(), GoloStatement.of(statement));
    }
    return this;
  }

  private void updateStateWith(GoloStatement<?> statement) {
    referenceTable.updateFrom(statement);
    makeParentOf(statement);
    checkForReturns(statement);
  }

  private void addStatement(int idx, GoloStatement<?> statement) {
    statements.add(idx, statement);
    updateStateWith(statement);
  }

  public Block prepend(Object statement) {
    if (statement != null) {
      this.addStatement(0, GoloStatement.of(statement));
    }
    return this;
  }

  private void setStatement(int idx, GoloStatement<?> statement) {
    statements.set(idx, statement);
    updateStateWith(statement);
  }

  /**
   * Merge nested blocks with this one if they don't define local variables.
   */
  public void flatten() {
    List<GoloStatement<?>> toFlatten = new ArrayList<>(statements);
    statements.clear();
    for (GoloStatement<?> statement : toFlatten) {
      if (statement instanceof Block) {
        Block block = (Block) statement;
        if (block.referenceTable.isEmpty()) {
          block.flatten();
          for (GoloStatement<?> s : block.statements) {
            this.add(s);
          }
          continue;
        }
      }
      this.add(statement);
    }
  }

  private void checkForReturns(GoloStatement<?> statement) {
    if (statement instanceof ReturnStatement || statement instanceof ThrowStatement) {
      hasReturn = true;
    } else if (statement instanceof ConditionalBranching) {
      hasReturn = hasReturn || ((ConditionalBranching) statement).returnsFromBothBranches();
    } else if (statement instanceof LoopStatement) {
      hasReturn = hasReturn || ((LoopStatement) statement).getBlock().hasReturn();
    } else if (statement instanceof TryCatchFinally) {
      hasReturn = hasReturn || ((TryCatchFinally) statement).hasReturn();
    }
  }

  public boolean hasReturn() {
    return hasReturn;
  }

  public int size() {
    return statements.size();
  }

  /**
   * Checks if this block contains only a return statement.
   */
  public boolean hasOnlyReturn() {
    return statements.size() == 1
           && statements.get(0) instanceof ReturnStatement
           && !((ReturnStatement) statements.get(0)).isReturningVoid();
  }

  /**
   * Checks if this blocks contains only an expression.
   */
  public boolean hasOnlyExpression() {
    return (statements.size() == 1 && statements.get(0) instanceof ExpressionStatement);
  }

  @Override
  public String toString() {
    return "{" + statements.toString() + "}";
  }

  public boolean isEmpty() {
    return statements.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBlock(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void walk(GoloIrVisitor visitor) {
    for (LocalReference ref : referenceTable.ownedReferences()) {
      ref.accept(visitor);
    }
    for (GoloStatement<?> statement : statements) {
      statement.accept(visitor);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return unmodifiableList(statements);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (statements.contains(original) && newElement instanceof GoloStatement) {
      setStatement(statements.indexOf(original), (GoloStatement) newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
