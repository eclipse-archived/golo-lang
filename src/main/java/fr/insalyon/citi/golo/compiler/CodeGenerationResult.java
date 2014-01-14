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

package fr.insalyon.citi.golo.compiler;

/**
 * A code generation result.
 * <p>
 * Compiling a single Golo source file may result in several JVM classes to be produced.
 * A <code>CodeGenerationResult</code> represents one such output.
 */
public final class CodeGenerationResult {

  private final byte[] bytecode;
  private final PackageAndClass packageAndClass;

  /**
   * Constructor for a code generation result.
   *
   * @param bytecode        the JVM bytecode as an array.
   * @param packageAndClass the package and class descriptor for the bytecode.
   */
  public CodeGenerationResult(byte[] bytecode, PackageAndClass packageAndClass) {
    this.bytecode = bytecode;
    this.packageAndClass = packageAndClass;
  }

  /**
   * @return the bytecode array.
   */
  public byte[] getBytecode() {
    return bytecode;
  }

  /**
   * @return the package and class descriptor.
   */
  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }
}
