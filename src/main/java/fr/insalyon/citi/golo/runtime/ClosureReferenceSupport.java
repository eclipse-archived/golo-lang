package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.genericMethodType;

public class ClosureReferenceSupport {

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, int arity) throws NoSuchMethodException, IllegalAccessException {
    MethodHandle target = caller.findStatic(caller.lookupClass(), name, genericMethodType(arity));
    return new ConstantCallSite(constant(MethodHandle.class, target));
  }
}
