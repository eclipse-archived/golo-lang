/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"golo"}, commandDescription = "Dynamically loads and runs from Golo source files")
public class GoloGoloCommand implements CliCommand {

  @Parameter(names = "--files", variableArity = true, description = "Golo source files (*.golo and directories). The last one has a main function or use --module", required = true)
  List<String> files = new LinkedList<>();

  @Parameter(names = "--module", description = "The Golo module with a main function")
  String module;

  @Parameter(names = "--args", variableArity = true, description = "Program arguments")
  List<String> arguments = new LinkedList<>();

  @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
  List<String> classpath = new LinkedList<>();

  public void execute() throws Throwable {
    URLClassLoader primaryClassLoader = primaryClassLoader(this.classpath);
    Thread.currentThread().setContextClassLoader(primaryClassLoader);
    GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
    Class<?> lastClass = null;
    for (String goloFile : this.files) {
      lastClass = loadGoloFile(goloFile, this.module, loader);
    }
    if (lastClass == null && this.module != null) {
      System.out.println("The module " + this.module + " does not exist in the classpath.");
      return;
    }
    callRun(lastClass, this.arguments.toArray(new String[this.arguments.size()]));
  }

  private Class<?> loadGoloFile(String goloFile, String module, GoloClassLoader loader) throws Throwable {
    File file = new File(goloFile);
    if (!file.exists()) {
      System.out.println("Error: " + file.getAbsolutePath() + " does not exist.");
    } else if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        Class<?> lastClass = null;
        for (File directoryFile : directoryFiles) {
          Class<?> loadedClass = loadGoloFile(directoryFile.getAbsolutePath(), module, loader);
          if (module == null || (loadedClass != null && loadedClass.getCanonicalName().equals(module))) {
            lastClass = loadedClass;
          }
        }
        return lastClass;
      }
    } else if (file.getName().endsWith(".golo")) {
      try (FileInputStream in = new FileInputStream(file)) {
        Class<?> loadedClass = loader.load(file.getName(), in);
        if (module == null || loadedClass.getCanonicalName().equals(module)) {
          return loadedClass;
        }
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    return null;
  }
}
