package fr.insalyon.citi.golo.cli;

import fr.insalyon.citi.golo.runtime.GoloClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

public class MainGoloGolo {

  public static void main(String... args) throws Throwable {
    if (args.length < 2) {
      System.out.println("No enough arguments given (at least a golo file and module).");
      return;
    }
    GoloClassLoader loader = new GoloClassLoader();
    for (int i = 0; i < (args.length - 1); i++) {
      File file = new File(args[i]);
      if (!file.exists()) {
        System.out.println("Error: " + file + " does not exist.");
        return;
      }
      if (!file.isFile()) {
        System.out.println("Error: " + file + " is not a file.");
        return;
      }
      try (FileInputStream in = new FileInputStream(file)) {
        loader.load(file.getName(), in);
      }
    }
    Class<?> module = loader.loadClass(args[args.length - 1]);
    Method main = module.getMethod("main", Object.class);
    main.invoke(null, new Object[]{new Object[]{}});
  }
}
