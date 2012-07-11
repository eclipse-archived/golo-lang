package gololang.internal.junit;

import java.io.File;
import java.io.FilenameFilter;
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

}
