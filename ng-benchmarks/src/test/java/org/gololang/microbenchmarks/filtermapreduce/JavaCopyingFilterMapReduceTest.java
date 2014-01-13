package org.gololang.microbenchmarks.filtermapreduce;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import static org.gololang.microbenchmarks.filtermapreduce.JavaCopyingFilterMapReduce.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JavaCopyingFilterMapReduceTest {

  @Test
  public void test_newWithSameType() throws Exception {
    assertEquals(ArrayList.class, newWithSameType(new ArrayList<>()).getClass());
    assertEquals(LinkedList.class, newWithSameType(new LinkedList<>()).getClass());
    assertEquals(HashSet.class, newWithSameType(new HashSet<>()).getClass());
  }

  @Test
  public void test_filter() throws Exception {
    ArrayList<Object> list = new ArrayList<Object>() {
      {
        add(1);
        add(2);
        add(3);
      }
    };
    Collection<?> result = filter(list, new JavaCopyingFilterMapReduce.Predicate() {
      @Override
      public boolean apply(Object object) {
        return (int) object % 2 == 1;
      }
    });
    assertArrayEquals(new Object[]{1, 3}, result.toArray());
  }

  @Test
  public void test_map() throws Exception {
    ArrayList<Object> list = new ArrayList<Object>() {
      {
        add(1);
        add(2);
        add(3);
      }
    };
    Collection<?> result = map(list, new JavaCopyingFilterMapReduce.Function() {
      @Override
      public Object apply(Object a) {
        return a.toString();
      }
    });
    assertArrayEquals(new Object[]{"1", "2", "3"}, result.toArray());
  }

  @Test
  public void test_reduce() throws Exception {
    ArrayList<Object> list = new ArrayList<Object>() {
      {
        add(1);
        add(2);
        add(3);
      }
    };
    Object result = reduce(list, 0, new BiFunction() {
      @Override
      public Object apply(Object a, Object b) {
        return (int) a + (int) b;
      }
    });
    assertEquals(6, result);
  }
}
