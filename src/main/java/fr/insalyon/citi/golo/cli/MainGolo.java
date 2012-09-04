package fr.insalyon.citi.golo.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.copyOfRange;

public class MainGolo {

  public static void main(String... args) throws InvocationTargetException, IllegalAccessException {
    if (args.length == 0) {
      System.out.println("No module given.");
      return;
    }
    try {
      Class<?> module = Class.forName(args[0]);
      Method main = module.getMethod("main", Object.class);
      Object[] mainArgs = (args.length == 1) ? new Object[]{} : copyOfRange(args, 1, args.length);
      main.invoke(null, (Object) mainArgs);
    } catch (ClassNotFoundException e) {
      System.out.println("The module " + args[0] + " could not be loaded.");
    } catch (NoSuchMethodException e) {
      System.out.println("The module " + args[0] + " does not have a main method with am argument.");
    }
  }
}
