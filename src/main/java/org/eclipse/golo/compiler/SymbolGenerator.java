/*
 * Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.compiler;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.Random;

/**
 * Name generator for synthetic objects.
 * <p>
 * The generated name follows the patter {@code __$$_<name>_<suffix>}.
 * The default name is {@code symbol}.
 * <p>
 * The generator maintains a stack of scope names, to generate hierarchical names.
 * <pre class="listing"><code class="lang-java" data-lang="java">
 * SymbolGenerator sym = new SymbolGenerator("closure");
 * sym.current(); // __$$_closure_0
 * sym.next(); // __$$_closure_1
 * sym.current(); // __$$_closure_1
 * sym.enter("scope");
 * sym.next(); // __$$_closure_scope_2
 * sym.enter("subscope");
 * sym.next(); // __$$_closure_scope_subscope_3
 * sym.exit().exit();
 * sym.next(); // __$$_closure_4
 * </code></pre>
 * <p>
 * Since the suffix generator maintains uniqueness, the name and scopes only purpose is to give
 * somewhat readable names to help debugging.
 * <p>
 * Be warned that the uniqueness is only preserved in the context of a single generator.
 * Two independent generators with the same name (and scope) can produce identical names.
 * <p>
 * A counter is used as default suffix instead of e.g. the generation timestamp or a random number to guarantee stability across
 * compilations to ease debugging.
 * <p>If a true uniqueness is required, or if the somewhat predictability of the symbol is a concern, one can use
 * {@link #getFor(Object)} or even {@link #next(Object)} in conjunction with {@code System.nanoTime()} or
 * {@code Random.nextLong()} (for instance
 * <code class="lang-java">sym.getFor(System.nanoTime())</code> or
 * <code class="lang-java">sym.next(Random.nextLong())</code>
 * ),
 * or just add such additional value as a generator scope (for instance
 * <code class="lang-java">sym.enter(Thread.currentThread().getId()).next()</code>).
 *
 * Moreover, a {@code UnaryOperator} can be provided in the constructor to customize the suffix generation.
 * Some helper functions are provided to create such operator, as well as scope suppliers.
 */
public final class SymbolGenerator implements Iterator<String> {
  public static final String PREFIX = "__$$_";
  public static final String DEFAULT_NAME = "symbol";
  public static final String ESCAPE_MANGLE = "$";
  public static final String JOIN = "_";
  private final Deque<String> prefixes = new LinkedBlockingDeque<>();
  private Supplier<?> scopeSupplier;
  private final UnaryOperator<Object> suffixUpdater;
  private final AtomicReference<Object> currentSuffix = new AtomicReference<>();

  public SymbolGenerator(String name, UnaryOperator<Object> suffixUpdater) {
    this.prefixes.addLast(escapeName(name));
    this.suffixUpdater = suffixUpdater;
    this.currentSuffix.updateAndGet(this.suffixUpdater);
  }

  public SymbolGenerator() {
    this(DEFAULT_NAME, counter());
  }

  public SymbolGenerator(String name) {
    this(name, counter());
  }

  public SymbolGenerator(UnaryOperator<Object> suffixUpdater) {
    this(DEFAULT_NAME, suffixUpdater);
  }

  /**
   * Returns a counter.
   *
   * Each call to this method returns a new independent counter.
   * This is the default suffix updater.
   */
  public static UnaryOperator<Object> counter() {
    AtomicLong counter = new AtomicLong();
    return (old) -> String.valueOf(counter.getAndIncrement());
  }

  /**
   * Returns a counter starting from the given value.
   *
   * Each call to this method returns a new independent counter.
   * Can be used as a suffix updater.
   */
  public static UnaryOperator<Object> counter(long start) {
    AtomicLong counter = new AtomicLong(start);
    return (old) -> String.valueOf(counter.getAndIncrement());
  }

  /**
   * Returns a random number.
   *
   * Each call to this method returns a new independent random generator.
   * Can be used as a suffix updater.
   */
  public static UnaryOperator<Object> random() {
    Random rnd = new Random();
    return (old) -> String.valueOf(rnd.nextLong());
  }

  /**
   * Returns a random number with the given seed.
   *
   * Each call to this method returns a new independent random generator.
   * Can be used as a suffix updater.
   */
  public static UnaryOperator<Object> random(long seed) {
    Random rnd = new Random(seed);
    return (old) -> String.valueOf(rnd.nextLong());
  }

  public static Supplier<String> scopeCounter() {
    AtomicLong counter = new AtomicLong();
    return () -> String.valueOf(counter.getAndIncrement());
  }

  /**
   * Returns the current thread ID as a String.
   *
   * Can be used as a scope supplier.
   * @see #withScopes(Supplier)
   */
  public static String threadId() {
    return String.valueOf(Thread.currentThread().getId());
  }

  /**
   * Returns the system time as a String.
   *
   * Can be used as a scope supplier.
   * @see #withScopes(Supplier)
   */
  public static String timedScopes() {
    return String.valueOf(System.nanoTime());
  }


  /**
   * Returns the system time as a String.
   *
   * Can be used as a suffix updater.
   */
  public static Object timedSuffixes(Object old) {
    return String.valueOf(System.nanoTime());
  }

  /**
   * Defines the supplier of scope names.
   *
   * When the {@code enter} method is called without providing a name for the new scope, the supplier denied here will
   * be used to generate the name.
   *
   * @param scopes an object supplier that returns the named used for the scope
   * @see #enter()
   * @see #timedScopes()
   * @see #threadId()
   */
  public SymbolGenerator withScopes(Supplier<Object> scopes) {
    this.scopeSupplier = scopes;
    return this;
  }

  /**
   * Minimal check for name validity.
   */
  public static String escapeName(Object name) {
    return name == null ? "" : String.valueOf(name).replace('.', '$').replace(' ', '_');
  }

  /**
   * Mangle a name using the given elements as components.
   */
  public static String mangle(Object... elements) {
    StringBuilder name = new StringBuilder(PREFIX);
    for (Object elt : elements) {
      String e = escapeName(elt);
      if (!"".equals(e)) {
        name.append(JOIN).append(e);
      }
    }
    return name.toString();
  }

  private String name(String localName, Object suffix) {
    return name(
        (localName == null || "".equals(localName)
          ? ""
          : (localName + JOIN))
        + String.valueOf(suffix));
  }

  private String name(String localName) {
    String name = PREFIX + String.join(JOIN, this.prefixes);
    if (localName != null && !"".equals(localName)) {
      name += JOIN + localName;
    }
    return name;
  }

  /**
   * Generates the next name for the current context.
   * <p>
   * Generates a suffix and returns the name generated by the current scope and suffix.
   *
   * @return the next name
   */
  public String next() {
    return next(null);
  }

  /**
   * Always true.
   *
   * Makes a {@code SymbolGenerator} an infinite {@code String} iterator.
   */
  public boolean hasNext() { return true; }

  /**
   * Generates the next name for the current context and given simple name.
   * <p>
   * Generates a suffix and returns the name generated by the given name and the current scope and suffix.
   *
   * <p><strong>Warning</strong>: only minimal check is made that the given name will produce a valid language symbol.
   * @param name the simple name to derive the unique name from; the value will be escaped
   * @return the corresponding next name
   * @see #escapeName(Object)
   */
  public String next(Object name) {
    return name(escapeName(name), this.currentSuffix.updateAndGet(this.suffixUpdater));
  }

  /**
   * Mangles the given name without using the generated suffix.
   *
   * <p>This can be used in macros to provide hygiene by mangling the local variable names. Mangling is escaped for
   * names beginning by {@code ESCAPE_MANGLE}.
   *
   * <p>For instance:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let symb = SymbolGenerator("foo")
   * symb: getFor("bar")  # __$$_foo_bar
   * symb: getFor("$bar") # bar
   * </code></pre>
   * <p><strong>Warning</strong>: only minimal check is made that the given name will produce a valid language symbol.
   *
   * @param name the simple name to derive the unique name from; the value will be escaped
   * @return the corresponding generated name
   * @see #escapeName(Object)
   */
  public String getFor(Object name) {
    String ln = escapeName(name);
    if (ln.startsWith(ESCAPE_MANGLE)) {
      return ln.substring(ESCAPE_MANGLE.length());
    }
    return name(ln);
  }

  /**
   * Generate the name for the current context and given simple name.
   * <p>
   * Returns the name generated by the given name and the current scope and suffix, without generating the next one.
   * <p><strong>Warning</strong>: only minimal check is made that the given name will produce a valid language symbol.
   *
   * @param name the simple name to derive the unique name from; the value will be escaped
   * @return the corresponding generated name
   *
   * @see #escapeName(Object)
   */
  public String current(Object name) {
    return name(escapeName(name), currentSuffix.get());
  }

  /**
   * Generate the name for the current context.
   * <p>
   * Returns the name generated by the current scope and counter, without
   * incrementing it.
   *
   * @return the generated name
   */
  public String current() {
    return name(null, currentSuffix.get());
  }

  /**
   * Exit from a scope.
   */
  public synchronized SymbolGenerator exit() {
    if (this.prefixes.size() > 1) {
      this.prefixes.removeLast();
    }
    return this;
  }

  /**
   * Enter a hierarchical scope.
   *
   * <p><strong>Warning</strong>: only minimal check is made that the given name will produce a valid language symbol.
   *
   * @param scopeName the name of the scope; the value will be escaped.
   * @return the generator itself
   *
   * @see #escapeName(Object)
   */
  public SymbolGenerator enter(Object scopeName) {
    String sn = escapeName(scopeName);
    if (!sn.isEmpty()) {
      this.prefixes.addLast(sn);
    }
    return this;
  }

  /**
   * Enter a hierarchical scope.
   *
   * The name of the scope is generated by the {@code java.util.function.Supplier<Object>} defined using
   * {@link #withScopes(Supplier)}
   *
   * @see #enter(Object)
   * @see #withScopes(Supplier)
   */
  public SymbolGenerator enter() {
    return enter(this.scopeSupplier.get());
  }
}
