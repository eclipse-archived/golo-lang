
package gololang;

/**
 * A singleton class that is equals to any object.
 * <p>
 * Used in unions matching special methods to ignore a value.
 */
public class Unknown {
  private static final Unknown instance = new Unknown();
  private Unknown() {}
  public static Unknown get() { 
    return instance;
  }

  @Override
  public boolean equals(Object o) {
    return o == null ? false : true;
  }

  @Override
  public int hashCode() { 
    return 0; 
  }
}
