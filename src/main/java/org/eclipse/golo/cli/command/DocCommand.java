/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.cli.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.parser.ASTCompilationUnit;
import org.eclipse.golo.compiler.parser.GoloOffsetParser;
import org.eclipse.golo.compiler.parser.ParseException;
import org.eclipse.golo.doc.AbstractProcessor;
import org.eclipse.golo.doc.CtagsProcessor;
import org.eclipse.golo.doc.HtmlProcessor;
import org.eclipse.golo.doc.MarkdownProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static gololang.Messages.*;

@Parameters(commandNames = {"doc"}, commandDescriptionKey = "doc", resourceBundle = "commands")
public class DocCommand implements CliCommand {

  @Parameter(names = "--format", descriptionKey = "doc.format", validateWith = DocFormatValidator.class)
  String format = "html";

  @Parameter(names = "--output", descriptionKey = "doc.output")
  String output = ".";

  @Parameter(descriptionKey = "source_files")
  List<String> sources = new LinkedList<>();

  private static final Map<String, AbstractProcessor> FORMATS = new HashMap<>();
  static {
    // TODO: use a service provider ?
    FORMATS.put("markdown", new MarkdownProcessor());
    FORMATS.put("html", new HtmlProcessor());
    FORMATS.put("ctags", new CtagsProcessor());
  }

  @Override
  public void execute() throws Throwable {

    AbstractProcessor processor = FORMATS.get(this.format);
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    for (String source : this.sources) {
      loadGoloFileCompilationUnit(source, units);
    }
    try {
      processor.process(units, Paths.get(this.output));
    } catch (Throwable throwable) {
      handleThrowable(throwable);
    }
  }

  private void loadGoloFileCompilationUnit(String goloFile, HashMap<String, ASTCompilationUnit> units) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          loadGoloFileCompilationUnit(directoryFile.getAbsolutePath(), units);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      try (FileInputStream in = new FileInputStream(goloFile)) {
        units.put(goloFile, new GoloOffsetParser(in).CompilationUnit());
      } catch (IOException e) {
        error(message("file_not_found", goloFile));
      } catch (ParseException e) {
        error(message("syntax_errors", goloFile, e.getMessage()));
      }
    }
  }

  public static class DocFormatValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      if (!FORMATS.keySet().contains(value)) {
        throw new ParameterException(message("format_error", FORMATS.keySet()));
      }
    }
  }
}
