package fr.insalyon.citi.golo.internal.testing;

public class Tracing {

  public static final boolean shouldTrace = System.getProperty("golo.test.trace", "no").equals("yes");

  public static void print(Object obj) {
    System.out.print(obj);
  }

  public static void println() {
    System.out.println();
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }
}
