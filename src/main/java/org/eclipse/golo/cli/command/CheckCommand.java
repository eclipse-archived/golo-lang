/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.GoloCompiler;

import static gololang.Messages.*;

@Parameters(commandNames = {"check"}, resourceBundle = "commands", commandDescriptionKey = "check")
public class CheckCommand implements CliCommand {

  @Parameter(names = {"--exit"}, descriptionKey = "check.exit")
  boolean exit = false;

  @Parameter(names = {"--verbose"}, descriptionKey = "check.verbose")
  boolean verbose = false;

  @Parameter(descriptionKey = "source_files")
  List<String> files = new LinkedList<>();

  @Override
  public void execute() throws Throwable {
    GoloCompiler compiler = new GoloCompiler();
    for (String file : files) {
      check(new File(file), compiler);
    }
  }

  private void check(File file, GoloCompiler compiler) {
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          check(directoryFile, compiler);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      try {
        if (verbose) {
          System.err.println(">>> " + message("check_info", file.getAbsolutePath()));
        }
        compiler.resetExceptionBuilder();
        compiler.check(compiler.parse(file.getAbsolutePath()));
      } catch (IOException e) {
        error(message("file_not_found", file));
      } catch (GoloCompilationException e) {
        handleCompilationException(e, exit);
      }
    }
  }
}

