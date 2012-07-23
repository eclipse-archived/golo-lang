package gololang.internal.junit;

import gololang.compiler.GoloCompiler;
import gololang.compiler.parser.ParseException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {

  public static List<Object[]> goloFilesIn(String path) {
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
    return data;
  }

  public static Class<?> compileAndLoadGoloModule(String sourceFolder, String goloFile, TemporaryFolder temporaryFolder, String moduleClass) throws IOException, ParseException, ClassNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    compiler.compileTo(goloFile, new FileInputStream(sourceFolder + goloFile), temporaryFolder.getRoot());
    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{temporaryFolder.getRoot().toURI().toURL()})) {
      return classLoader.loadClass(moduleClass);
    }
  }
}
