/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.compiler.ir;

import org.eclipse.golo.compiler.PackageAndClass;

import java.util.Set;
import org.eclipse.golo.compiler.parser.GoloASTNode;
import org.eclipse.golo.compiler.ClosureCaptureGoloIrVisitor;


import static org.eclipse.golo.compiler.ir.Builders.*;

public final class Struct extends TypeWithMembers {

  public static final String IMMUTABLE_FACTORY_METHOD = "$_immutable";

  private PackageAndClass moduleName;

  @Override
  public Struct ofAST(GoloASTNode node) {
    super.ofAST(node);
    return this;
  }

  Struct(String name) {
    super(name);
  }

  @Override
  public PackageAndClass getPackageAndClass() {
    return moduleName.createSubPackage("types").createSubPackage(getName());
  }

  // TODO: refactor to use the parent node
  public void setModuleName(PackageAndClass module) {
    this.moduleName = module;
  }

  private GoloFunction createDefaultConstructor() {
    GoloFunction defaultFactory = functionDeclaration(getName()).synthetic()
      .returns(call(getFactoryDelegateName())
        .withArgs(getFullDefaultArgs()));
    defaultFactory.accept(new ClosureCaptureGoloIrVisitor());
    if (defaultFactory.getSyntheticParameterCount() > 0) {
      // we use a dependant default value. The default factory must raise an exception
      defaultFactory = functionDeclaration(getName()).synthetic()
        .block(call("raise").withArgs(constant(
                "Can't call the default constructor of a structure with dependant default value.")));
    }
    return defaultFactory;
  }

  private String getImmutableName() {
    return "Immutable" + getName();
  }

  private GoloFunction createFullArgsImmutableConstructor() {
    return functionDeclaration(getImmutableName()).synthetic()
      .withParameters(getMemberNames())
      .returns(call(getFactoryDelegateName() + "." + IMMUTABLE_FACTORY_METHOD).withArgs(getFullArgs()));
  }

  private GoloFunction createDefaultArgsImmutableConstructor() {
    return functionDeclaration(getImmutableName()).synthetic()
      .withParameters(getNonDefaultMemberNames())
      .returns(call(getFactoryDelegateName() + "." + IMMUTABLE_FACTORY_METHOD).withArgs(getDefaultArgs()));
  }

  public Set<GoloFunction> createFactories() {
    Set<GoloFunction> factories = super.createFactories();
    factories.add(createDefaultConstructor());
    factories.add(createFullArgsImmutableConstructor());
    if (hasDefaults()) {
      factories.add(createDefaultArgsImmutableConstructor());
    }
    return factories;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitStruct(this);
  }
}
