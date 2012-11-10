package fr.insalyon.citi.golo.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Module {

  static String[] imports(Class<?> callerClass) {
    String[] imports;
    try {
      Method $imports = callerClass.getMethod("$imports");
      imports = (String[]) $imports.invoke(null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $imports() static method.
      imports = new String[]{};
    }
    return imports;
  }
}
