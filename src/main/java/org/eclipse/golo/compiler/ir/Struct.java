/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.PackageAndClass;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.golo.compiler.parser.GoloASTNode;

import static java.util.Arrays.asList;
import static org.eclipse.golo.compiler.ir.Builders.*;

public final class Struct extends GoloElement {

  public static final String IMMUTABLE_FACTORY_METHOD = "$_immutable";

  private PackageAndClass moduleName;
  private final String name;
  private final Set<String> members = new LinkedHashSet<>();
  private final Set<String> publicMembers = new LinkedHashSet<>();

  @Override
  public Struct ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  Struct(String name) {
    super();
    this.name = name;
  }

  public Struct members(String... members) {
    return this.members(asList(members));
  }

  public Struct members(Collection<String> members) {
    for (String member : members) {
      this.addMember(member);
    }
    return this;
  }

  public void addMember(String name) {
    this.members.add(name);
    if (!name.startsWith("_")) {
      publicMembers.add(name);
    }
  }

  public PackageAndClass getPackageAndClass() {
    return new PackageAndClass(moduleName.toString() + ".types", name);
  }

  public void setModuleName(PackageAndClass module) {
    this.moduleName = module;
  }

  public Set<String> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public Set<String> getPublicMembers() {
    return Collections.unmodifiableSet(publicMembers);
  }

  public Set<GoloFunction> createFactories() {
    String fullName = getPackageAndClass().toString();
    return new LinkedHashSet<GoloFunction>(asList(
        functionDeclaration(name).synthetic().block(returns(call(fullName))),

        functionDeclaration(name).synthetic()
        .withParameters(members)
        .block(
          returns(call(fullName)
            .withArgs(members.stream().map(ReferenceLookup::new).toArray()))),

        functionDeclaration("Immutable" + name).synthetic()
        .withParameters(members)
        .block(
          returns(call(fullName + "." + IMMUTABLE_FACTORY_METHOD)
            .withArgs(members.stream().map(ReferenceLookup::new).toArray())))));
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitStruct(this);
  }

  @Override
  public void walk(GoloIrVisitor visitor) {
    // nothing to do, not a composite
  }

  @Override
  protected void replaceElement(GoloElement original, GoloElement newElement) {
    throw cantReplace();
  }
}
