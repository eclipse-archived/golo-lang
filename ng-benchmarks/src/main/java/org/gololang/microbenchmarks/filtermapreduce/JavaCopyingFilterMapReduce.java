package org.gololang.microbenchmarks.filtermapreduce;

import java.util.*;

public class JavaCopyingFilterMapReduce {

  public static interface Predicate {
    boolean apply(Object object);
  }

  public static interface Function {
    Object apply(Object a);
  }

  public static interface BiFunction {
    Object apply(Object a, Object b);
  }

  public static Collection<Object> newWithSameType(Collection<?> collection) {
    if (collection instanceof RandomAccess) {
      return new ArrayList<>();
    } else if (collection instanceof List) {
      return new LinkedList<>();
    } else if (collection instanceof Set) {
      return new HashSet<>();
    } else {
      throw new RuntimeException("Not a supported collection: " + collection.getClass());
    }
  }

  public static Collection<?> filter(Collection<?> collection, Predicate predicate) {
    Collection<Object> result = newWithSameType(collection);
    for (Object obj : collection) {
      if (predicate.apply(obj)) {
        result.add(obj);
      }
    }
    return result;
  }

  public static Collection<?> map(Collection<?> collection, Function fun) {
    Collection<Object> result = newWithSameType(collection);
    for (Object obj : collection) {
      result.add(fun.apply(obj));
    }
    return result;
  }

  public static Object reduce(Collection<?> collection, Object initialValue, BiFunction reducer) {
    Object result = initialValue;
    for (Object obj : collection) {
      result = reducer.apply(result, obj);
    }
    return result;
  }
}
