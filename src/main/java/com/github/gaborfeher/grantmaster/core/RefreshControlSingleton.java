package com.github.gaborfeher.grantmaster.core;

public class RefreshControlSingleton {
  private static RefreshControlSingleton instance;

  public static synchronized RefreshControlSingleton getInstance() {
    if (instance == null) {
      instance = new RefreshControlSingleton();
    }
    return instance;
  }
  
  /**
   * true if somewhere in the GUI an item is opened for editing. This is
   * erased by a refresh.
   */
  private boolean editingActive;
  
  public boolean isEditingActive() {
    return editingActive;
  }
  
  public void setEditingActive(boolean editingActive) {
    this.editingActive = editingActive;
  }
}
