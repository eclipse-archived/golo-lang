package fr.insalyon.citi.golo.cli;

import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.parser.TokenMgrError;
import fr.insalyon.citi.golo.runtime.GoloClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MainGoloGolo {

  public static void main(String... args) throws Throwable {
    if (args.length < 1) {
      System.out.println("Usage: gologolo <.golo files> [--args arg1 arg2 ...]");
      System.out.println("(the last .golo file has a main function)");
      System.out.println();
      System.out.println("Examples:");
      System.out.println("  golo foo.golo bar.golo");
      System.out.println("  golo foo.golo bar.golo --args hello world");
      System.out.println();
      return;
    }
    GoloClassLoader loader = new GoloClassLoader();
    Class<?> lastClass = null;
    int i = 0;
    for (; i < args.length; i++) {
      if ("--args".equals(args[i])) {
        break;
      }
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
        lastClass = loader.load(file.getName(), in);
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      } catch (TokenMgrError e) {
        handleTokenMgrError(e);
      }
    }
    Object[] appArgs;
    if (i < args.length) {
      appArgs = Arrays.copyOfRange(args, i + 1, args.length);
    } else {
      appArgs = new Object[]{};
    }
    Method main = lastClass.getMethod("main", Object.class);
    main.invoke(null, new Object[]{appArgs});
  }

  static void handleTokenMgrError(TokenMgrError e) {
    System.out.println("[error] " + e.getMessage());
    System.exit(1);
  }

  static void handleCompilationException(GoloCompilationException e) {
    if (e.getCause() != null) {
      System.out.println("[error] " + e.getCause().getMessage());
    }
    for (GoloCompilationException.Problem problem : e.getProblems()) {
      System.out.println("[error] " + problem.getDescription());
    }
    System.exit(1);
  }
}
