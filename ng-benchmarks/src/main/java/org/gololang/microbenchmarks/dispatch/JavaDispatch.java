package org.gololang.microbenchmarks.dispatch;

public class JavaDispatch {

  public String dispatch(Object[] data) {
    String result = "";
    for (Object object : data) {
      result = object.toString();
    }
    return result;
  }
}
