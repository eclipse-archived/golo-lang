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
import com.beust.jcommander.converters.FileConverter;

import org.eclipse.golo.cli.GolofilesManager;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompiler;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static gololang.Messages.*;

@Parameters(commandNames = "golo", resourceBundle = "commands", commandDescriptionKey = "golo")
public final class GoloGoloCommand implements CliCommand {

  @Parameter(names = "--files", variableArity = true, descriptionKey = "golo.files", required = true, converter = FileConverter.class)
  List<File> files = new LinkedList<>();

  @Parameter(names = "--module", descriptionKey = "main_module")
  String module;

  @Parameter(names = "--args", variableArity = true, descriptionKey = "arguments")
  List<String> arguments = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();

  @Override
  public void execute() throws Throwable {
    GoloClassLoader loader = classpath.initGoloClassLoader();
    GoloCompiler compiler = loader.getCompiler();
    Class<?> lastClass = GolofilesManager.goloFiles(this.files)
      .filter(this::canRead)
      .map(wrappedTreatment(compiler::parse))
      .map(wrappedTreatment(compiler::transform))
      .sorted(CliCommand.MODULE_COMPARATOR)
      .map(wrappedTreatment(compiler::expand))
      .map(wrappedTreatment(compiler::refine))
      .map(wrappedTreatment(compiler::generate))
      .filter(Objects::nonNull)
      .flatMap(Collection::stream)
      .map(displayInfo("Loading %s"))
      .map(loader::load)
      .reduce(null, this::selectMainModule);

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

  private Class<?> selectMainModule(Class<?> old, Class<?> loaded) {
    if (this.module == null || this.module.equals(loaded.getCanonicalName())) {
      return loaded;
    }
    return old;
  }
}
