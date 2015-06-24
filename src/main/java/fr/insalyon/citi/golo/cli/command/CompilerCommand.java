/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"compile"}, commandDescription = "Compiles Golo source files")
public class CompilerCommand implements CliCommand {

  @Parameter(names = "--output", description = "The compiled classes output directory")
  String output = ".";

  @Parameter(description = "Golo source files (*.golo)")
  List<String> sources = new LinkedList<>();

  @Override
  public void execute() throws Throwable {
    GoloCompiler compiler = new GoloCompiler();
    File outputDir = new File(this.output);
    for (String source : this.sources) {
      File file = new File(source);
      try (FileInputStream in = new FileInputStream(file)) {
        compiler.compileTo(file.getName(), in, outputDir);
      } catch (IOException e) {
        System.out.println("[error] " + source + " does not exist or could not be opened.");
        return;
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
  }
}
