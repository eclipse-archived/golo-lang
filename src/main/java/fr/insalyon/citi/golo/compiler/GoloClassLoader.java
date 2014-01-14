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

import java.io.InputStream;
import java.util.List;

/**
 * Provides a facility to dynamically load Golo source code and access the generated code from a dedicated class loader.
 * <p>
 * Golo source files can be compiled and the resulting JVM bytecode be injected into the class loader. It is important
 * to note that this class loader definition is not thread safe.
 * <p>
 * This class loader does not support reloading. Attempts to load source files that may produce the same bytecode
 * definitions will resulting in exceptions.
 */
public class GoloClassLoader extends ClassLoader {

  private final GoloCompiler compiler = new GoloCompiler();

  /**
   * Creates a class loader from a parent.
   *
   * @param parent the parent classloader.
   */
  public GoloClassLoader(ClassLoader parent) {
    super(parent);
  }

  /**
   * Creates a class loader from the default parent.
   */
  public GoloClassLoader() {
    super();
  }

  /**
   * Compiles and loads the resulting JVM bytecode for a Golo source file.
   *
   * @param goloSourceFilename    the source file name.
   * @param sourceCodeInputStream the source input stream.
   * @return the class matching the Golo module defined in the source.
   * @throws GoloCompilationException if either of the compilation phase failed.
   */
  public synchronized Class<?> load(String goloSourceFilename, InputStream sourceCodeInputStream) throws GoloCompilationException {
    List<CodeGenerationResult> results = compiler.compile(goloSourceFilename, sourceCodeInputStream);
    Class<?> lastClassIsModule = null;
    for (CodeGenerationResult result : results) {
      byte[] bytecode = result.getBytecode();
      lastClassIsModule = defineClass(null, bytecode, 0, bytecode.length);
    }
    return lastClassIsModule;
  }
}
