
/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang;

import org.testng.annotations.Test;
import org.eclipse.golo.internal.testing.GoloTest;
import java.util.List;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

public class ControlTest extends GoloTest {

  private static final class DummyClose implements AutoCloseable {
    private final List<Object> lst;
    private final String closeFails;
    DummyClose(List<Object> lst, String closeFails) {
      this.lst = lst;
      this.closeFails = closeFails;
    }

    public void close() {
      if (this.closeFails != null) {
        throw new RuntimeException(this.closeFails);
      }
      this.lst.add("closed");
    }
  }

  public static AutoCloseable dummyClose(List<Object> l, String f) {
    return new DummyClose(l, f);
  }

  private static final class DummyLock implements Lock {
    private final List<Object> lst;
    private final String unlockFails;
    DummyLock(List<Object> l, String f) {
      this.lst = l;
      this.unlockFails = f;
    }

    public void lock() {
      this.lst.add("locked");
    }

    public void unlock() {
      if (this.unlockFails != null) {
        throw new RuntimeException(this.unlockFails);
      }
      this.lst.add("unlocked");
    }
    public void lockInterruptibly() { this.lock(); }
    public Condition newCondition() { throw new UnsupportedOperationException(); }
    public boolean tryLock() { this.lock(); return true; }
    public boolean tryLock(long time, TimeUnit unit) { return this.tryLock(); }
  }

  public static Lock dummyLock(List<Object> l, String f) {
    return new DummyLock(l, f);
  }

  @Override
  public String srcDir() {
    return "for-test/";
  }

  @Test
  public void test() throws Throwable {
    run("within-contexts");
  }

}
