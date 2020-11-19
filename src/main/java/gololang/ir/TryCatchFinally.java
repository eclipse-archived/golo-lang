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

  public static final String DUMMY_TRY_RESULT_VARIABLE = "__$$_result";
  private static final String DUMMY_EXCEPTION_VARIABLE = "__$$_exception";
  private String exceptionId;
  private LocalReference dummyException;
  private Block tryBlock;
  private Block catchBlock;
  private Block finallyBlock;

  private TryCatchFinally() {
    super();
    this.exceptionId = null;
  }

  /**
   * Creates an empty {@code try catch finally} statement.
   */
  public static TryCatchFinally tryCatch() {
    return new TryCatchFinally();
  }


  /**
   * Creates a complete {@code try catch finally} statement.
   * <p>
   * Less readable than the fluent API, but useful for meta-generation.
   */
  public static TryCatchFinally create(String exceptionName, GoloElement<?> tryBlock, GoloElement<?> catchBlock, GoloElement<?> finallyBlock) {
    return new TryCatchFinally()
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
    if (!this.hasCatchBlock()) {
      return this.dummyException.getIndex();
    }
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
    this.dummyException = LocalReference.generate(DUMMY_EXCEPTION_VARIABLE).synthetic();
    this.finallyBlock = makeParentOf(Block.of(block));
    this.finallyBlock.getReferenceTable().add(this.dummyException);
    return this;
  }

  public String dummyExceptionName() {
    return this.dummyException.getName();
  }

  public boolean hasFinallyBlock() {
    return finallyBlock != null;
  }

  public boolean hasCatchBlock() {
    return catchBlock != null;
  }

  /**
   * Checks if a return is present in the try or catch block and a finally is present.
   * <p>
   * In this case we must not generate a return but a jump to the finally block.
   */
  public boolean mustJumpToFinally() {
    return (this.tryBlock.hasReturn()
          || (this.catchBlock != null && this.catchBlock.hasReturn()))
      && this.finallyBlock != null;

  }

  public boolean hasReturn() {
    return (this.tryBlock.hasReturn()
            && (this.catchBlock == null || this.catchBlock.hasReturn()))
        || (this.finallyBlock != null
            && this.finallyBlock.hasReturn());
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
