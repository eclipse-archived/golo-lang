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
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.cli.GolofilesManager;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;


@Parameters(commandNames = "compile", resourceBundle = "commands", commandDescriptionKey = "compile")
public final class CompilerCommand implements CliCommand {

  @Parameter(names = "--output", descriptionKey = "compile.output")
  String output = ".";

  @Parameter(descriptionKey = "source_files", converter = FileConverter.class)
  LinkedList<File> sources = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();

  @Parameter(names = "--verbose", descriptionKey = "verbose")
  boolean verbose = false;

  @Override
  public boolean verbose() {
    return this.verbose;
  }

  @Override
  public void execute() throws Throwable {
    GoloClassLoader loader = classpath.initGoloClassLoader();
    GoloCompiler compiler = loader.getCompiler();
    try (GolofilesManager fm = GolofilesManager.of(this.output)) {
      GolofilesManager.goloFiles(this.sources)
        .filter(this::canRead)
        .map(wrappedTreatment(compiler::parse))
        .map(wrappedTreatment(compiler::transform))
        .sorted(CliCommand.MODULE_COMPARATOR)
        .map(displayInfo("Compiling %s"))
        .map(wrappedTreatment(compiler::expand))
        .map(wrappedTreatment(compiler::refine))
        .map(wrappedTreatment(compiler::generate))
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .forEach(r -> {
          loader.load(r);
          fm.save(r);
        });
    }
  }
}
