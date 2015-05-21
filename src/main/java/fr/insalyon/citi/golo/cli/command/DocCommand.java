/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.doc.AbstractProcessor;
import fr.insalyon.citi.golo.doc.CtagsProcessor;
import fr.insalyon.citi.golo.doc.HtmlProcessor;
import fr.insalyon.citi.golo.doc.MarkdownProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"doc"}, commandDescription = "Generate documentation from Golo source files")
public class DocCommand implements CliCommand {

  @Parameter(names = "--format", description = "Documentation output format (html, markdown, ctags)", validateWith = DocFormatValidator.class)
  String format = "html";

  @Parameter(names = "--output", description = "The documentation output directory. With ctags format, '-' can be used for standard output (e.g. when executed in an editor)")
  String output = ".";

  @Parameter(description = "Golo source files (*.golo or directories)")
  List<String> sources = new LinkedList<>();

  @Override
  public void execute() throws Throwable {

    AbstractProcessor processor;
    switch (this.format) {
      case "markdown":
        processor = new MarkdownProcessor();
        break;
      case "html":
        processor = new HtmlProcessor();
        break;
      case "ctags":
        processor = new CtagsProcessor();
        break;
      default:
        throw new AssertionError("WTF?");
    }
    HashMap<String, ASTCompilationUnit> units = new HashMap<>();
    for (String source : this.sources) {
      loadGoloFileCompilationUnit(source, units);
    }
    try {
      processor.process(units, Paths.get(this.output));
    } catch (Throwable throwable) {
      System.out.println("[error] " + throwable.getMessage());
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
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      } catch (ParseException e) {
        System.out.println("[error] " + goloFile + " has syntax errors: " + e.getMessage());
      }
    }
  }

  public static class DocFormatValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "html":
        case "markdown":
        case "ctags":
          return;
        default:
          throw new ParameterException("Output format must be in: {html, markdown, ctags}");
      }
    }
  }
}
