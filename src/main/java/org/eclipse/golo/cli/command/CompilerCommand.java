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

  private File outputDir;
  private JarOutputStream jar;

  private void saveToJar(CodeGenerationResult result) throws IOException {
    String entryName = result.getPackageAndClass().packageName().replaceAll("\\.", "/");
    if (!entryName.isEmpty()) {
      entryName += "/";
    }
    entryName = entryName + result.getPackageAndClass().className() + ".class";
    this.jar.putNextEntry(new ZipEntry(entryName));
    this.jar.write(result.getBytecode());
    this.jar.closeEntry();
  }

  private void saveToClass(CodeGenerationResult result) throws IOException {
    // TODO: Move this logic into the CodeGenerationResult (or PackageAndClass, or both)
    File outputFolder = new File(this.outputDir, result.getPackageAndClass().packageName().replaceAll("\\.", "/"));
    if (!outputFolder.exists() && !outputFolder.mkdirs()) {
      throw new IOException(message("directory_not_created", outputFolder));
    }
    File outputFile = new File(outputFolder, result.getPackageAndClass().className() + ".class");
    try (FileOutputStream out = new FileOutputStream(outputFile)) {
      out.write(result.getBytecode());
    }
  }

  private void save(List<CodeGenerationResult> results) {
    for (CodeGenerationResult result : results) {
      try {
        if (compilingToJar()) {
          saveToJar(result);
        } else {
          saveToClass(result);
        }
      } catch (IOException e) {
        error(e.getLocalizedMessage());
      }
    }
  }

  private boolean initOutput() throws IOException {
    this.outputDir = compilingToJar() ? null : new File(this.output);
    this.jar = compilingToJar() ? new JarOutputStream(new FileOutputStream(new File(this.output)), manifest()) : null;
    return outputDir != null && outputDir.isFile();

  }

  private boolean compilingToJar() {
    return this.output.endsWith(".jar");
  }

  @Override
  public void execute() throws Throwable {
    // TODO: recurse into directories
    GoloCompiler compiler = classpath.initGoloClassLoader().getCompiler();
    if (!initOutput()) {
      error(message("file_exists", outputDir));
      return;
    }
    for (String source : this.sources) {
      File sourceFile = new File(source);
      if (!this.canReadFile(sourceFile)) { continue; }
      try {
        this.save(compiler.compile(sourceFile));
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      } catch (Throwable e) {
        handleThrowable(e);
      }
    }
    if (compilingToJar()) {
      this.jar.close();
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
