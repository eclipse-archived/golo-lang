/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang.error;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import gololang.Tuple;
import gololang.FunctionReference;

/**
 * A container object which represent the result of a maybe failing operation.
 *
 * <p>This object is used when chaining computations (e.g. using map-filter operations) that can
 * produce an error. Instead of raising an exception, the operation can use this object to
 * encapsulate the result. This is similar to {@code Optional}, but also encapsulate the type of
 * error in the form of a {@code Throwable} instance that can be raised later.
 *
 * <p>This is similar to the {@code Either} or {@code Result} type in other functional languages (e.g.
 * <a href="https://hackage.haskell.org/package/base/docs/Data-Either.html">Haskell</a>,
 * <a href="https://doc.rust-lang.org/std/result/">Rust</a> or
 * <a href="http://www.scala-lang.org/api/2.9.3/scala/Either.html">Scala</a>)
 *
 * <p>Typical usage:<ul>
 * <li>return {@link #empty()} instead of returning {@code null},
 * <li>use {@link #error(java.lang.Throwable)} or {@link #fail(java.lang.String)} instead of
 * throwing an exception,
 * <li>use {@link #ok(java.lang.Object)} to return a normal value.
 * </ul>
 */
public final class Result<T, E extends Throwable> implements Iterable<T> {

  private static final Result<?, ?> EMPTY = new Result<>();
  private final T value;
  private final E error;

  private Result() {
    this.value = null;
    this.error = null;
  }

  private Result(T value, E throwable) {
    this.value = value;
    this.error = throwable;
  }

  /**
   * Dynamic polymorphic constructor.
   *
   * <p>Dynamically dispatch on {@link #empty()}, {@link #error(java.lang.Throwable)},
   * {@link #option(java.util.Optional)} or {@link #ok(java.lang.Object)} depending on the
   * {@code value} type. This is mainly useful in Golo code.
   *
   * @param value the value to encapsulate
   * @return a {@code Result} representing the value
   */
  public static Result<Object, Throwable> of(Object value) {
    if (value == null) {
      return empty();
    }
    if (value instanceof Throwable) {
      return error((Throwable) value);
    }
    if (value instanceof Optional) {
      @SuppressWarnings("unchecked")
      Optional<Object> opt = (Optional<Object>) value;
      return option(opt);
    }
    return ok(value);
  }

  /**
   * Returns an empty {@code Result}.
   *
   * <p>Represents a successful computation that returns nothing.
   *
   * @return an empty {@code Result}
   */
  public static <T, E extends Throwable> Result<T, E> empty() {
    @SuppressWarnings("unchecked")
    Result<T, E> r = (Result<T, E>) EMPTY;
    return r;
  }

  /**
   * Returns a valid {@code Result}.
   *
   * <p>Represents a successful computation's result.
   *
   * @param <T> the class of the value
   * @param value the possibly-null value to describe
   * @return a {@code Result} containing the value if the specified value is non-null,
   * otherwise an empty {@code Result}
   */
  public static <T, E extends Throwable> Result<T, E> ok(T value) {
    return value == null ? empty() : new Result<>(value, null);
  }

  /**
   * Returns a failed {@code Result}.
   *
   * <p>Represent a computation that failed.
   *
   * @param <E> the class of the throwable
   * @param throwable the error that occurred
   * @return a {@code Result} containing the throwable
   */
  public static <T, E extends Throwable> Result<T, E> error(E throwable) {
    return throwable == null ? empty() : new Result<>(null, throwable);
  }

  /**
   * Construct a {@code Result} from a {@code Optional}.
   *
   * @param <T> the class of the value
   * @param opt the {@code Optional} representing the possibly present value
   * @return a {@code Result} containing the value if {@code isPresent()} is {@code true},
   * otherwise an empty {@code Result}
   */
  public static <T, E extends Throwable> Result<T, E> option(Optional<T> opt) {
    return opt == null || !opt.isPresent() ? empty() : new Result<>(opt.get(), null);
  }

    /**
   * Construct a {@code Result} from a {@code Optional}.
   *
   * @param <T> the class of the value
   * @param opt the {@code Optional} representing the possibly present value
   * @param message a message used to create an error if the {@code Optional} is empty
   * @return a {@code Result} containing the value if {@code isPresent()} is {@code true},
   * otherwise an error containing {@code NoSuchElementException}
   */
  public static <T> Result<T, NoSuchElementException> option(Optional<T> opt, String message) {
    return (opt == null || !opt.isPresent())
      ? new Result<>(null, new NoSuchElementException(message))
      : new Result<>(opt.get(), null);
  }

  /**
   * Returns a failed {@code Result}.
   *
   * <p>Represent a computation that failed. This is similar to {@link #error(java.lang.Throwable)}
   * but only the message is provided, and a {@code RuntimeException} is automatically created.
   *
   * @param message the message representing the error
   * @return a {@code Result} containing a {@code RuntimeException}
   */
  public static <T> Result<T, RuntimeException> fail(String message) {
    return error(new RuntimeException(message));
  }

  /**
   * If a value is present, returns the value, if empty throws {@code NoSuchElementException},
   * otherwise throws the contained error.
   *
   * @return the non-null value contained in this {@code Result}
   * @throws NoSuchElementException if the {@code Result} is empty
   * @throws E if the {@code Result} is an error
   */
  public T get() throws E, NoSuchElementException {
    if (value != null) {
      return value;
    }
    if (error != null) {
      throw error;
    }
    throw new NoSuchElementException("Empty result");
  }

  /**
   * Convert this {@code Result} into a {@code Optional} describing its value.
   *
   * @return an {@code Optional} containing the value of this {@code Result},
   * or an empty {@code Optional} if {@code isValue()} is {@code false}
   */
  public Optional<T> toOptional() {
    if (value != null) {
      return Optional.of(value);
    }
    return Optional.empty();
  }

  /**
   * Convert this {@code Result} into a {@link java.util.List} of values.
   *
   * @return an singleton list containing the value if present, otherwise an empty list
   */
  public List<T> toList() {
    if (value != null) {
      return Collections.singletonList(value);
    }
    return Collections.emptyList();
  }

  /**
   * Convert this {@code Result} into a {@link java.util.List} of error.
   *
   * @return an singleton list containing the error if present, otherwise an empty list
   */
  public List<E> toErrorList() {
    if (error != null) {
      return Collections.singletonList(error);
    }
    return Collections.emptyList();
  }


  @Override
  public Iterator<T> iterator() {
    return toList().iterator();
  }

  /**
   * Convert this {@code Result} into a {@code Optional} describing its error.
   *
   * @return an {@code Optional} containing the error of this {@code Result},
   * or an empty {@code Optional} if {@code isError()} is {@code false}
   */
  public Optional<E> toOptionalError() {
    if (error != null) {
      return Optional.of(error);
    }
    return Optional.empty();
  }

  /**
   * Return the value of present, otherwise return {@code other}.
   *
   * @param other the value to return if is empty
   * @return the value if present, otherwise {@code other}
   */
  public T orElse(T other) {
    if (value != null) {
      return value;
    }
    return other;
  }

  /**
   * Return the value of present, otherwise return the result of the invocation of {@code fun}.
   *
   * @param fun the function to invoke if is empty (may return a default value or throw an
   * exception)
   * @return the value if present, otherwise the invocation of {@code fun}
   */
  public Object orElseGet(FunctionReference fun) throws Throwable {
    if (value != null) {
      return value;
    }
    return fun.invoke();
  }


  /**
   * @return {@code true} if there is neither a value nor an error, otherwise {@code false}
   */
  public boolean isEmpty() {
    return value == null && error == null;
  }

  /**
   * @return {@code true} if there is an error (and no value), otherwise {@code false}
   */
  public boolean isError() {
    return value == null && error != null;
  }

  /**
   * @param type the class to test the error for
   * @return {@code true} if the present error is an instance of {@code type}
   */
  public boolean isError(Class<?> type) {
    return error != null && type.isInstance(error);
  }

  /**
   * @return {@code true} if there is a value (and no error), otherwise {@code false}
   */
  public boolean isValue() {
    return value != null && error == null;
  }

  /**
   * @param val the value to test for presence
   * @return {@code true} if the present value is equal to {@code val}
   */
  public boolean isValue(Object val) {
    return Objects.equals(value, val);
  }

  /**
   * If a value is present, apply the provided mapping function to it, otherwise return the
   * {@code Result} itself. If the application succeed with a value, return a {@code Result}
   * containing it, if the result is null, return an empty {@code Result}, otherwise return a
   * {@code Result} capturing the {@code Throwable} that was thrown.
   *
   * @param <U> The type of the result of the mapping function
   * @param mapper a mapping function to apply to the value, if present
   * @return a {@code Result} describing the result of applying the mapping function to the value of
   * this {@code Result}
   * @throws NullPointerException if the mapping function is null
   */
  public <U, X extends Throwable> Result<U, X> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (value == null) {
      @SuppressWarnings("unchecked")
      Result<U, X> r = (Result<U, X>) this;
      return r;
    }
    try {
      return ok(mapper.apply(value));
    } catch (Throwable e) {
      @SuppressWarnings("unchecked")
      Result<U, X> r = (Result<U, X>) error(e);
      return r;
    }
  }

  /**
   * If this result is an error, apply the provided mapping function to the contained error,
   * otherwise return the {@code Result} itself.
   * If the application succeed with a value, return a {@code Result}
   * containing it, if the result is null, return an empty {@code Result}, otherwise return a
   * {@code Result} capturing the {@code Throwable} that was thrown.
   *
   * @param <X> The type of the result of the mapping function
   * @param mapper a mapping function to apply to the error, if present
   * @return a {@code Result} describing the result of applying the mapping function to the error of
   * this {@code Result}
   * @throws NullPointerException if the mapping function is null
   */
  public <X extends Throwable> Result<T, X> mapError(Function<? super E, ? extends X> mapper) {
    Objects.requireNonNull(mapper);
    if (error == null) {
      @SuppressWarnings("unchecked")
      Result<T, X> r = (Result<T, X>) this;
      return r;
    }
    try {
      return error(mapper.apply(error));
    } catch (Throwable e) {
      @SuppressWarnings("unchecked")
      Result<T, X> r = (Result<T, X>) error(e);
      return r;
    }
  }



  /**
   * If a value is present, apply the provided {@code Result}-bearing mapping function to it,
   * otherwise return the {@code Result} itself.
   * If the application succeed, return its result, otherwise return a
   * {@code Result} capturing the {@code Throwable} that was thrown.
   *
   * @param <U> The type of the value of the {@code Result} returned by the mapping function
   * @param mapper a mapping function to apply to the value, if present
   * @return the result of applying the mapping function to the value of this {@code Result}
   * @throws NullPointerException if the mapping function is {@code null} or if it returns {@code null}
   */
  public <U, X extends Throwable> Result<U, X> flatMap(Function<? super T, Result<U, X>> mapper) {
    Objects.requireNonNull(mapper);
    if (isEmpty() || isError()) {
      @SuppressWarnings("unchecked")
      Result<U, X> r = (Result<U, X>) this;
      return r;
    }
    Result<U, X> result;
    try {
      result = mapper.apply(value);
    } catch (Throwable e) {
      @SuppressWarnings("unchecked")
      Result<U, X> err = (Result<U, X>) error(e);
      return err;
    }
    return Objects.requireNonNull(result);
  }

  /**
   * Golo compatible version of {@code flatMap}.
   *
   * See <a href="https://github.com/eclipse/golo-lang/issues/277">issue 277</a>
   */
  public Result<Object, Throwable> flatMap(FunctionReference mapper) {
    @SuppressWarnings("unchecked")
    Result<Object, Throwable> result = (Result<Object, Throwable>) flatMap((Function) mapper.to(Function.class));
    return result;
  }

  /**
   * Remove one level of result.
   * <p>
   * This is actually equivalent to {@code flatMap(identity)}
   * (or {@code r.flatMap(f)} is equivalent to {@code r.map(f).flattened()})
   * <p>
   * For instance:
   * <pre><code>
   * ok(ok(42)).flattened() == ok(42)
   * fail("error").flattened() == fail("error")
   * empty().flattened() == empty()
   * </code></pre>
   *
   * @return the value contained in this result if it's a result
   * @throws ClassCastException when the result does not contain a result.
   */
  public Result<?, ?> flattened() {
    if (value == null) {
      return this;
    }
    return (Result) value;
    // }
    // throw new ClassCastException(String.format("%s cannot be cast to %s",
    //       value.getClass(), Result.class));
  }

  /**
   * Same as {@code map} or {@code flatMap} depending on the type returned by {@code mapper}.
   * <p>
   * This is a generic version for {@code map} and {@code flatMap}:
   * if {@code mapper} returns a {@code Result}, it's equivalent to {@code flatMap},
   * otherwise, it's equivalent to {@code map}.
   * <p>
   * This allows code such as:
   * <pre><code>
   * Ok(21): andThen(|x| -> x + 1): andThen(|x| -> Ok(2 * x)) == Ok(42)
   * </code></pre>
   */
  public Result<? extends Object, ? extends Throwable> andThen(FunctionReference mapper) {
    Objects.requireNonNull(mapper);
    if (isEmpty() || isError()) {
      return this;
    }
    Object result;
    try {
      result = mapper.invoke(value);
    } catch (Throwable e) {
      return error(e);
    }
    if (result instanceof Result) {
      return (Result<?, ?>) result;
    }
    else {
      return ok(result);
    }
  }

  /**
   * Case analysis for the result.
   * <p>
   * If the result is a value, apply the first function to it;
   * if it is an error, apply the second function to it.
   * <p>
   * Note that if the result is empty, i.e. the value is {@code null},
   * the {@code mapping} function is applied to {@code null}.
   *
   * @param mapping the function to apply to the contained value
   * @param recover the function to apply to the contained error
   * @return the result of applying the corresponding function
   */
  public Object either(FunctionReference mapping, FunctionReference recover) throws Throwable {
    if (isError()) {
      return recover.invoke(error);
    }
    return mapping.invoke(value);
  }

    /**
   * Three way case analysis for the result.
   * <p>
   * If the result is a value, apply the first function to it;
   * if it is an error, apply the second function to it;
   * if it is empty, invoke the third function.
   *
   * @param mapping the function to apply to the contained value
   * @param recover the function to apply to the contained error
   * @param def the function to invoke if the result is empty (takes no arguments)
   * @return the result of applying the corresponding function
   */
  public Object either(FunctionReference mapping, FunctionReference recover, FunctionReference def) throws Throwable {
    if (isEmpty()) {
      return def.invoke();
    }
    return this.either(mapping, recover);
  }

  /**
   * Golo compatible version of {@code map}.
   *
   * See <a href="https://github.com/eclipse/golo-lang/issues/277">issue 277</a>
   */
  public Result<Object, Throwable> map(FunctionReference mapper) {
    @SuppressWarnings("unchecked")
    Result<Object, Throwable> result = (Result<Object, Throwable>) map((Function) mapper.to(Function.class));
    return result;
  }

  /**
   * If a value is present and matches the given predicate, return a {@code Result} describing the
   * value, otherwise return an empty {@code Result}. If the {@code Result} is empty or is an error,
   * return the {@code Result} itself.
   *
   * @param predicate a predicate to apply to the value, if present
   * @return a {@code Result} describing the value of this {@code Result} if it maches the predicate
   * @throws NullPointerException if the predicate is null
   */
  public Result<T, E> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    if (isEmpty() || isError()) {
      return this;
    }
    return predicate.test(value) ? this : empty();
  }

  /**
   * Reduce {@code this} using {@code func} with {@code init} as initial value.
   * <p>
   * For instance:
   * <pre><code>
   * Result.ok("b"): reduce("a", |x, y| -> x + y) == "ab"
   * Result.empty(): reduce(42, |x, y| -> x + y) == 42
   * Result.fail("error"): reduce(42, |x, y| -> x + y) == 42
   * </code></pre>
   * @param init the initial value
   * @param func the aggregation function
   * @return the initial value if this is not a value, the aggregated result otherwise
   */
  public Object reduce(Object init, FunctionReference func) throws Throwable {
    if (value == null) {
      return init;
    }
    return func.invoke(init, value);
  }

  /**
   * Apply the function contained is this result to the given result.
   * <p>
   * If the function has several parameters, a result containing a partialized version
   * is returned, that can be `apply`ed to subsequent results.
   * This makes `Result` an “applicative functor”.
   */
  public Result<?, ?> apply(Result<?, ?> other) throws Throwable {
    if (!this.isValue()) {
      return this;
    }
    if (!(value instanceof FunctionReference)) {
      throw new RuntimeException("The result must contain a function to be applied");
    }
    FunctionReference f = (FunctionReference) value;
    if (f.arity() > 1) {
      return ok(f.bindTo(other.get()));
    }
    return other.map((FunctionReference) value);
  }

  /**
   * Conjunctive chaining.
   *
   * @param other the other result
   * @return {@code other} if this result is a value, otherwise {@code this}
   */
  public Result<?, ?> and(Result<?, ?> other) {
    if (!this.isError()) {
      return other;
    }
    return this;
  }

  /**
   * Disjunctive chaining.
   *
   * @param other the other result
   * @return {@code other} if this result is an error, otherwise {@code this}
   */
  public Result<?, ?> or(Result<?, ?> other) {
    if (this.isError()) {
      return other;
    }
    return this;
  }


  /**
   * Indicate whether some other object is equal to this {@code Result}.
   * The other object is considered equal if:
   * <ul>
   * <li>it is also a {@code Result} and;
   * <li>both instances are empty or;
   * <li>both instances have values that are equal via {@code equals()} or;
   * <li>both instances are errors of the same type with the same message.
   * </ul>
   *
   * @param o an object to be tested for equality
   * @return {@code true} if the other object is equal to this object, otherwise {@code false}
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (this.getClass() != o.getClass()) {
      return false;
    }
    Result<?, ?> that = (Result<?, ?>) o;
    return Objects.equals(this.value, that.value)
      && (Objects.equals(this.error, that.error)
          || (this.error.getClass() == that.error.getClass()
              && this.error.getMessage().equals(that.error.getMessage())));
  }

  @Override
  public int hashCode() {
    if (error == null) {
      return Objects.hash(value);
    }
    return Objects.hash(error.getClass(), error.getMessage());
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "Result.empty";
    }
    if (isError()) {
      return String.format("Result.error[%s]", error);
    }
    return String.format("Result.value[%s]", value);
  }

  /**
   * Return a {@link gololang.Tuple} representing this {@code Result}.
   *
   * <p>Return a 2-tuple containing the error and the value contained by this {@code Result}, so
   * that it can be used in a destructuring golo assignment. The first value is the error, and the
   * second is the correct value (mnemonic: “right” also means “correct”). For instance:
   * <pre><code>
   * let e, v = Result.ok(42)        # e is null and v is 42
   * let e, v = Result.empty()       # e is null and v is null
   * let e, v = Result.fail("error") # e is RuntimeException("error") and v is null
   * </code></pre>
   * This allows to deal with error in the same way as Go does for instance.
   *
   * @return a 2-tuple containing the error and the value contained by this {@code Result}
   */
  public Tuple destruct() {
    return new Tuple(error, value);
  }

}

