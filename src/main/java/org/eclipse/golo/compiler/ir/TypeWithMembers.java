/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.golo.compiler.PackageAndClass;

import static org.eclipse.golo.compiler.ir.Builders.functionDeclaration;
import static org.eclipse.golo.compiler.ir.Builders.call;
import static java.util.stream.Collectors.toList;

/**
 * An abstract class for struct-like types.
 */
abstract class TypeWithMembers extends GoloElement {
  private final Set<Member> members = new LinkedHashSet<>();
  private final String name;

  TypeWithMembers(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected String getFactoryDelegateName() {
    return getFullName();
  }

  public abstract PackageAndClass getPackageAndClass();

  String getFullName() {
    return getPackageAndClass().toString();
  }

  public TypeWithMembers members(Object... members) {
    for (Object member : members) {
      withMember(member);
    }
    return this;
  }

  public boolean hasMembers() {
    return !this.members.isEmpty();
  }

  protected void addMember(Member member) {
    this.members.add(member);
    makeParentOf(member);
  }

  void addMembers(Iterable<Member> members) {
    members.forEach(this::addMember);
  }

  public TypeWithMembers withMember(Object member) {
    if (member instanceof Member) {
      addMember((Member) member);
    } else {
      addMember(new Member(member.toString()));
    }
    return this;
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
      .map(ReferenceLookup::new)
      .toArray();
  }

  public Set<GoloFunction> createFactories() {
    Set<GoloFunction> factories = new LinkedHashSet<>();
    factories.add(createFullArgsConstructor());
    return factories;
  }

  protected GoloFunction createFullArgsConstructor() {
    return functionDeclaration(getName()).synthetic()
      .withParameters(getMemberNames())
      .returns(call(getFactoryDelegateName()).withArgs(getFullArgs()));
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    for (Member m : members) {
      m.accept(visitor);
    }
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }
}
