
package gololang;

/**
 * A singleton class that is equals to any object.
 * <p>
 * Used in special matching methods to ignore a value.
 * Note that this <strong>break the commutativity</strong> property of {@code equals}, and thus this object must be
 * used with caution.
 */
public final class Unknown {

  private static final Unknown INSTANCE = new Unknown();

  private Unknown() { }

  public static Unknown get() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object o) {
    return o != null;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
