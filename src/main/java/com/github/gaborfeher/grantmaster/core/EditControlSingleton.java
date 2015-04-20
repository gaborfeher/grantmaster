package com.github.gaborfeher.grantmaster.core;

public class EditControlSingleton {
  private static EditControlSingleton instance;
  
  public static synchronized EditControlSingleton getInstance() {
    if (instance == null) {
      instance = new EditControlSingleton();
    }
    return instance;
  }
}
