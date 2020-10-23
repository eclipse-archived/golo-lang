/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.cli;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.function.Consumer;

import org.eclipse.golo.compiler.CodeGenerationResult;
import org.eclipse.golo.cli.command.Metadata;
import static gololang.Messages.*;

/**
 * Helper class to deal with Golo files.
 *
 * <p>Ease the finding, loading and saving of golo source files and compilation result.
 */
public class GolofilesManager implements AutoCloseable, Consumer<CodeGenerationResult> {

  private File outputDir;
  private JarOutputStream jar;
  private boolean recurse = false;
  private boolean compilingToJar = false;

  private GolofilesManager() {}

  private void saveToJar(CodeGenerationResult result) throws IOException {
    this.jar.putNextEntry(new ZipEntry(result.getOutputFilename()));
    this.jar.write(result.getBytecode());
    this.jar.closeEntry();
  }

  private void saveToClass(CodeGenerationResult result) throws IOException {
    File outputFile = new File(this.outputDir, result.getOutputFilename());
    File outputFolder = outputFile.getParentFile();
    if (!outputFolder.exists() && !outputFolder.mkdirs()) {
      throw new IOException(message("directory_not_created", outputFolder));
    }
    try (FileOutputStream out = new FileOutputStream(outputFile)) {
      out.write(result.getBytecode());
    }
  }

  public void accept(CodeGenerationResult result) {
    save(result);
  }

  public void save(CodeGenerationResult result) {
    try {
      if (this.compilingToJar) {
        saveToJar(result);
      } else {
        saveToClass(result);
      }
    } catch (IOException e) {
      error(e.getLocalizedMessage());
    }
  }

  public void saveAll(List<CodeGenerationResult> results) {
    for (CodeGenerationResult result : results) {
      this.save(result);
    }
  }

  public void close() throws IOException {
    if (this.jar != null) {
      this.jar.close();
    }
  }

  private static Manifest manifest() {
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.put(new Attributes.Name("Created-By"), "Eclipse Golo " + Metadata.VERSION);
    return manifest;
  }

  public static GolofilesManager of(String output) throws IOException {
    if (output.endsWith(".jar")) {
      return withOutputJar(new File(output));
    }
    return withOutputDir(new File(output));
  }

  public static GolofilesManager withOutputJar(File output) throws IOException{
    GolofilesManager fm = new GolofilesManager();
    fm.jar = new JarOutputStream(new FileOutputStream(output), manifest());
    fm.compilingToJar = true;
    return fm;

  }

  public static GolofilesManager withOutputDir(File outputDir) throws IOException {
    if (outputDir != null && outputDir.isFile()) {
      throw new IOException(message("file_exists", outputDir));
    }
    GolofilesManager fm = new GolofilesManager();
    fm.outputDir = outputDir;
    return fm;
  }

}
