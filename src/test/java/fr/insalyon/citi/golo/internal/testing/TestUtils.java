/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.internal.testing;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import org.testng.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {

  public static Iterator<Object[]> goloFilesIn(String path) {
    List<Object[]> data = new LinkedList<>();
    File[] files = new File(path).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".golo");
      }
    });
    for (File file : files) {
      data.add(new Object[]{file});
    }
    return data.iterator();
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile) throws IOException, ParseException, ClassNotFoundException {
    return compileAndLoadGoloModule(sourceFolder, goloFile, new GoloClassLoader(TestUtils.class.getClassLoader()));
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile, GoloClassLoader goloClassLoader) throws IOException, ParseException, ClassNotFoundException {
    try {
      return goloClassLoader.load(goloFile, new FileInputStream(sourceFolder + goloFile));
    } catch (GoloCompilationException e) {
      for (GoloCompilationException.Problem p : e.getProblems()) {
        Reporter.log("In " + goloFile + ": " + p.getDescription(), shouldTestNgReportToConsole());
      }
      throw e;
    }
  }

  private static boolean shouldTestNgReportToConsole() {
    return Boolean.valueOf(System.getProperty("testng-report-to-console", "false"));
  }
}
