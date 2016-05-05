/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gololang;

import java.util.LinkedList;

/**
 * Models a thread-safe observable variable.
 */
public final class Observable {

  private volatile Object value;

  private final Object lock = new Object();
  private final LinkedList<Observer> observers = new LinkedList<>();

  /**
   * Creates a new observable from an initial value.
   *
   * @param initialValue the initial value.
   */
  public Observable(Object initialValue) {
    this.value = initialValue;
  }

  /**
   * Gets the current value.
   *
   * @return the current value.
   */
  public Object get() {
    return value;
  }

  /**
   * Changes the current value and notifies all observers.
   *
   * @param newValue the new value.
   */
  public void set(Object newValue) {
    synchronized (lock) {
      this.value = newValue;
      for (Observer observer : observers) {
        observer.apply(newValue);
      }
    }
  }

  /**
   * Registers an observer.
   *
   * @param observer an observer.
   * @return this observable object.
   */
  public Observable onChange(Observer observer) {
    synchronized (lock) {
      observers.add(observer);
    }
    return this;
  }

  /**
   * Creates an observer that filters the values of this observable.
   *
   * @param predicate a predicate function.
   * @return an observer whose values filter this observable.
   */
  public Observable filter(final Predicate predicate) {
    final Observable observable = new Observable(null);
    this.onChange(new Observer() {
      @Override
      public void apply(Object newValue) {
        if (predicate.apply(newValue)) {
          observable.set(newValue);
        }
      }
    });
    return observable;
  }

  /**
   * Creates an observer that maps the values of this observable.
   *
   * @param function a mapping function.
   * @return an observer whose values map this observable.
   */
  public Observable map(final Function function) {
    final Observable observable = new Observable(null);
    this.onChange(new Observer() {
      @Override
      public void apply(Object newValue) {
        observable.set(function.apply(newValue));
      }
    });
    return observable;
  }

  @Override
  public String toString() {
    return "Observable{" +
        "value=" + value +
        '}';
  }

  public interface Function {
    Object apply(Object value);
  }

  public interface Predicate {
    boolean apply(Object value);
  }

  public interface Observer {
    void apply(Object newValue);
  }
}
