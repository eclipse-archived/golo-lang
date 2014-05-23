/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.compiler.ir;

public class FunctionInvocation extends AbstractInvocation {

  private boolean onReference = false;
  private boolean onModuleState = false;
  private boolean anonymous = false;

  public FunctionInvocation() {
    super("anonymous");
    anonymous = true;
  }

  public FunctionInvocation(String name) {
    super(name);
  }

  public boolean isOnReference() {
    return onReference;
  }

  public void setOnReference(boolean onReference) {
    this.onReference = onReference;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public boolean isOnModuleState() {
    return onModuleState;
  }

  public void setOnModuleState(boolean onModuleState) {
    this.onModuleState = onModuleState;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitFunctionInvocation(this);
  }
}
