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

import java.util.Objects;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@code try catch finally} statement.
 *
 * <p>Represents nodes such as:
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * try {
 *   # try block
 * } catch (ex) {
 *   # catch block
 * } finally {
 *   # finally block
 * }
 * </code></pre>
 */
public final class TryCatchFinally extends GoloStatement<TryCatchFinally> {

  private String exceptionId;
  private Block tryBlock;
  private Block catchBlock;
  private Block finallyBlock;

  private TryCatchFinally(String exceptionId) {
    super();
    this.exceptionId = exceptionId;
  }

  /**
   * Creates an empty {@code try catch finally} statement.
   */
  public static TryCatchFinally tryCatch() {
    return new TryCatchFinally(null);
  }


  /**
   * Creates a complete {@code try catch finally} statement.
   * <p>
   * Less readable than the fluent API, but useful for meta-generation.
   */
  public static TryCatchFinally create(String exceptionName, GoloElement<?> tryBlock, GoloElement<?> catchBlock, GoloElement<?> finallyBlock) {
    return new TryCatchFinally(null)
      .trying(tryBlock)
      .catching(exceptionName, catchBlock)
      .finalizing(finallyBlock);
  }

  protected TryCatchFinally self() { return this; }

  /**
   * Get the exception name.
   *
   * <p>{@code ex} in the previous example.
   */
  public String getExceptionId() {
    return exceptionId;
  }

  /**
   * Returns the internal index of the exception reference.
   */
  public int getExceptionRefIndex() {
    return this.catchBlock.getReferenceTable().get(this.exceptionId).getIndex();
  }

  public Block getTryBlock() {
    return tryBlock;
  }

  /**
   * Defines the try block.
   *
   * <p>This is a builder method.
   *
   * @param block a {@link Block} or a statement that will be wrapped.
   * @see Block#of(Object)
   */
  public TryCatchFinally trying(Object block) {
    this.tryBlock = makeParentOf(Block.of(block));
    return this;
  }

  public Block getCatchBlock() {
    return catchBlock != null ? catchBlock : Block.nullBlock();
  }

  /**
   * Defines the catch block.
   *
   * <p>This is a builder method.
   *
   * @param exceptionId the name of the catched exception instance.
   * @param block a {@link Block} or a statement that will be wrapped.
   * @see Block#of(Object)
   */
  public TryCatchFinally catching(String exceptionId, Object block) {
    this.exceptionId = exceptionId;
    return catching(block);
  }

  public TryCatchFinally catching(Object block) {
    if (block == null) {
      this.catchBlock = null;
      this.exceptionId = null;
      return this;
    }
    this.catchBlock = makeParentOf(Block.of(block));
    this.catchBlock.getReferenceTable().add(LocalReference.of(exceptionId).synthetic());
    return this;
  }

  public Block getFinallyBlock() {
    return finallyBlock != null ? finallyBlock : Block.nullBlock();
  }

  /**
   * Defines the finally block.
   *
   * <p>This is a builder method.
   *
   * @param block a {@link Block} or a statement that will be wrapped.
   * @see Block#of(Object)
   */
  public TryCatchFinally finalizing(Object block) {
    this.finallyBlock = makeParentOf(Block.of(block));
    return this;
  }

  public boolean hasFinallyBlock() {
    return finallyBlock != null;
  }

  public boolean hasCatchBlock() {
    return catchBlock != null;
  }

  /**
   * Check if this statement has both a catch block and a finally block.
   */
  public boolean isTryCatchFinally() {
    return hasCatchBlock() && hasFinallyBlock();
  }

  /**
   * Check if this statement has only a catch block.
   */
  public boolean isTryCatch() {
    return hasCatchBlock() && !hasFinallyBlock();
  }

  /**
   * Check if this statement has only a finally block.
   */
  public boolean isTryFinally() {
    return !hasCatchBlock() && hasFinallyBlock();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitTryCatchFinally(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    LinkedList<GoloElement<?>> children = new LinkedList<>();
    children.add(tryBlock);
    if (catchBlock != null) {
      children.add(catchBlock);
    }
    if (finallyBlock != null) {
      children.add(finallyBlock);
    }
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(original, tryBlock)) {
      trying(newElement);
    } else if (Objects.equals(original, catchBlock)) {
      catching(newElement);
    } else if (Objects.equals(original, finallyBlock)) {
      finalizing(newElement);
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
