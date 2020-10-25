/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static gololang.Messages.*;

@Parameters(commandNames = {"golo"}, resourceBundle = "commands", commandDescriptionKey = "golo")
public class GoloGoloCommand implements CliCommand {

  @Parameter(names = "--files", variableArity = true, descriptionKey = "golo.files", required = true)
  List<String> files = new LinkedList<>();

  @Parameter(names = "--module", descriptionKey = "main_module")
  String module;

  @Parameter(names = "--args", variableArity = true, descriptionKey = "arguments")
  List<String> arguments = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();

  @Override
  public void execute() throws Throwable {
    GoloClassLoader loader = classpath.initGoloClassLoader();
    Class<?> lastClass = null;
    for (String goloFile : this.files) {
      lastClass = loadGoloFile(goloFile, this.module, loader);
    }
    if (lastClass == null && this.module != null) {
      error(message("module_not_found", this.module));
      return;
    }
    if (lastClass == null) {
      return;
    }
    try {
      callRun(lastClass, this.arguments.toArray(new String[this.arguments.size()]));
    } catch (NoMainMethodException e) {
      error(message("module_no_main", lastClass.getName()));
    }
  }

  private Class<?> loadGoloFile(String goloFile, String module, GoloClassLoader loader) throws Throwable {
    File file = new File(goloFile);
    if (!file.exists()) {
      error(message("file_not_found", file));
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
      try {
        Class<?> loadedClass = loader.load(file);
        if (module == null || loadedClass.getCanonicalName().equals(module)) {
          return loadedClass;
        }
      } catch (IOException e) {
        error(message("file_not_found", file.getAbsolutePath()));
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    return null;
  }
}
