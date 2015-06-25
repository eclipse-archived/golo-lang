/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.IrTreeDumper;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.GoloOffsetParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"diagnose"}, commandDescription = "Diagnosis for the Golo compiler internals")
public class DiagnoseCommand implements CliCommand {

  @Parameter(names = "--tool", description = "The diagnosis tool to use: {ast, ir}", validateWith = DiagnoseModeValidator.class)
  String mode = "ir";

  @Parameter(description = "Golo source files (*.golo and directories)")
  List<String> files = new LinkedList<>();

  @Override
  public void execute() throws Throwable {

    try {
      switch (this.mode) {
        case "ast":
          dumpASTs(this.files);
          break;
        case "ir":
          dumpIRs(this.files);
          break;
        default:
          throw new AssertionError("WTF?");
      }
    } catch (GoloCompilationException e) {
      handleCompilationException(e);
    }
  }


  private void dumpASTs(List<String> files) {
    GoloCompiler compiler = new GoloCompiler();
    for (String file : files) {
      dumpAST(file, compiler);
    }
  }

  private void dumpAST(String goloFile, GoloCompiler compiler) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          dumpAST(directoryFile.getAbsolutePath(), compiler);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      System.out.println(">>> AST for: " + goloFile);
      try (FileInputStream in = new FileInputStream(goloFile)) {
        ASTCompilationUnit ast = compiler.parse(goloFile, new GoloOffsetParser(in));
        ast.dump("% ");
        System.out.println();
      } catch (IOException e) {
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      }
    }
  }

  private void dumpIRs(List<String> files) {
    GoloCompiler compiler = new GoloCompiler();
    IrTreeDumper dumper = new IrTreeDumper();
    for (String file : files) {
      dumpIR(file, compiler, dumper);
    }
  }

  private void dumpIR(String goloFile, GoloCompiler compiler, IrTreeDumper dumper) {
    File file = new File(goloFile);
    if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        for (File directoryFile : directoryFiles) {
          dumpIR(directoryFile.getAbsolutePath(), compiler, dumper);
        }
      }
    } else if (file.getName().endsWith(".golo")) {
      System.out.println(">>> IR for: " + file);
      try (FileInputStream in = new FileInputStream(goloFile)) {
        ASTCompilationUnit ast = compiler.parse(goloFile, new GoloOffsetParser(in));
        GoloModule module = compiler.check(ast);
        dumper.visitModule(module);
        System.out.println();
      } catch (IOException e) {
        System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
      }
    }
  }

  public static class DiagnoseModeValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      switch (value) {
        case "ast":
        case "ir":
          return;
        default:
          throw new ParameterException("Diagnosis tool must be in: {ast, ir}");
      }
    }
  }


}
