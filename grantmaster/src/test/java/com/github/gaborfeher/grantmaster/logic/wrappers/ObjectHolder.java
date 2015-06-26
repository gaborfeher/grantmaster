package com.github.gaborfeher.grantmaster.logic.wrappers;

/**
 * Stores an object. Makes it possible to access it from inside anonymous
 * inner classes or lambda expressions.
 */
public class ObjectHolder <T extends Object> {
  private T obj = null;
  public void set(T obj) {
    this.obj = obj;
  }
  public T get() {
    return obj;
  }
}
