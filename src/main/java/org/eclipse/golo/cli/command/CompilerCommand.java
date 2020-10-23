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
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.CodeGenerationResult;
import org.eclipse.golo.cli.GolofilesManager;
import gololang.error.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static gololang.Messages.*;

@Parameters(commandNames = {"compile"}, resourceBundle = "commands", commandDescriptionKey = "compile")
public class CompilerCommand implements CliCommand {

  @Parameter(names = "--output", descriptionKey = "compile.output")
  String output = ".";

  @Parameter(descriptionKey = "source_files")
  List<String> sources = new LinkedList<>();

  @ParametersDelegate
  ClasspathOption classpath = new ClasspathOption();


  @Override
  public void execute() throws Throwable {
    // TODO: recurse into directories
    GoloCompiler compiler = classpath.initGoloClassLoader().getCompiler();
    try(GolofilesManager fm = GolofilesManager.of(this.output)) {
      for (String source : this.sources) {
        File sourceFile = new File(source);
        if (!this.canReadFile(sourceFile)) { continue; }
        try {
          fm.saveAll(compiler.compile(sourceFile));
        } catch (GoloCompilationException e) {
          handleCompilationException(e);
        } catch (Throwable e) {
          handleThrowable(e);
        }
      }
    }
  }
}
