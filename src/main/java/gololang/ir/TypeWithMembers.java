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

import static gololang.ir.GoloFunction.function;
import static java.util.stream.Collectors.toList;

/**
 * An abstract class for struct-like types.
 */
public abstract class TypeWithMembers<T extends TypeWithMembers<T>> extends GoloType<T> {
  private final Set<Member> members = new LinkedHashSet<>();

  TypeWithMembers(String name) {
    super(name);
  }

  protected String getFactoryDelegateName() {
    return getFullName();
  }

  String getFullName() {
    return getPackageAndClass().toString();
  }

  /**
   * Adds all members to this type.
   *
   * @see #withMember(Object)
   */
  public T members(Object... members) {
    for (Object member : members) {
      withMember(member);
    }
    return self();
  }

  public boolean hasMembers() {
    return !this.members.isEmpty();
  }

  protected void addMember(Member member) {
    this.members.add(makeParentOf(member));
  }

  void addMembers(Iterable<Member> members) {
    members.forEach(this::addMember);
  }

  /**
   * Adds a member to this type.
   *
   * <p>This is a builder method.
   *
   * @param member a {@link Member} or any object whose {@code toString} method is used to create a member object.
   */
  public T withMember(Object member) {
    addMember(Member.of(member));
    return self();
  }

  protected List<String> getMemberNames() {
    return members.stream()
      .map(Member::getName)
      .collect(toList());
  }

  public Set<Member> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public List<Member> getPublicMembers() {
    return members.stream()
      .filter(Member::isPublic)
      .collect(toList());
  }

  protected Object[] getFullArgs() {
    return members.stream()
      .map(Member::getName)
      .map(ReferenceLookup::of)
      .toArray();
  }

  /**
   * Creates the factory functions for this type.
   *
   * <p>Internal API
   */
  public Set<GoloFunction> createFactories() {
    Set<GoloFunction> factories = new LinkedHashSet<>();
    factories.add(createFullArgsConstructor());
    return factories;
  }

  protected GoloFunction createFullArgsConstructor() {
    return function(getName()).synthetic()
      .withParameters(getMemberNames())
      .returns(FunctionInvocation.of(getFactoryDelegateName()).withArgs(getFullArgs()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return new ArrayList<>(members);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
