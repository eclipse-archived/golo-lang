package fr.insalyon.citi.golo.internal.testing;

import fr.insalyon.citi.golo.compiler.GoloCompiler;
import fr.insalyon.citi.golo.compiler.parser.ParseException;
import fr.insalyon.citi.golo.runtime.GoloClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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
    GoloClassLoader goloClassLoader = new GoloClassLoader(TestUtils.class.getClassLoader());
    return goloClassLoader.load(goloFile, new FileInputStream(sourceFolder + goloFile));
  }
}
