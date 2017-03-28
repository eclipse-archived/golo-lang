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
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloCompilationException;
import org.eclipse.golo.compiler.GoloCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static gololang.Messages.*;

@Parameters(commandNames = {"compile"}, resourceBundle = "commands", commandDescriptionKey = "compile")
public class CompilerCommand implements CliCommand {

  @Parameter(names = "--output", descriptionKey = "compile.output")
  String output = ".";

  @Parameter(descriptionKey = "source_files")
  List<String> sources = new LinkedList<>();

  @Override
  public void execute() throws Throwable {
    // TODO: recurse into directories
    GoloCompiler compiler = new GoloCompiler();
    final boolean compilingToJar = this.output.endsWith(".jar");
    File outputDir = compilingToJar ? null : new File(this.output);
    JarOutputStream jarOutputStream = compilingToJar ? new JarOutputStream(new FileOutputStream(new File(this.output)), manifest()) : null;
    for (String source : this.sources) {
      File file = new File(source);
      try (FileInputStream in = new FileInputStream(file)) {
        if (compilingToJar) {
          compiler.compileToJar(file.getName(), in, jarOutputStream);
        } else {
          compiler.compileTo(file.getName(), in, outputDir);
        }
      } catch (IOException e) {
        error(message("file_not_found", source));
        return;
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    if (compilingToJar) {
      jarOutputStream.close();
    }
  }

  private Manifest manifest() {
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.put(new Attributes.Name("Created-By"), "Eclipse Golo " + Metadata.VERSION);
    return manifest;
  }
}
