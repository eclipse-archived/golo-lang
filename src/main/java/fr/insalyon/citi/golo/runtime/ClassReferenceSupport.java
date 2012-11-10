package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static fr.insalyon.citi.golo.runtime.Module.imports;
import static java.lang.invoke.MethodHandles.constant;

public class ClassReferenceSupport {

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) throws ClassNotFoundException {
    String className = name.replaceAll("#", "\\.");
    Class<?> callerClass = caller.lookupClass();
    ClassLoader classLoader = callerClass.getClassLoader();

    Class<?> classRef = tryLoading(className, classLoader);
    if (classRef == null) {
      for (String importClassName : imports(callerClass)) {
        classRef = tryLoading(importClassName + "." + className, classLoader);
        if (classRef != null) {
          break;
        }
      }
    }

    if (classRef != null) {
      return new ConstantCallSite(constant(Class.class, classRef));
    }
    throw new ClassNotFoundException("Dynamic resolution failed for name: " + name);
  }

  private static Class<?> tryLoading(String name, ClassLoader classLoader) {
    try {
      return Class.forName(name, true, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
