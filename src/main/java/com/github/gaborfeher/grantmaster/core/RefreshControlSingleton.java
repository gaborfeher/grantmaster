package com.github.gaborfeher.grantmaster.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RefreshControlSingleton {
  private static RefreshControlSingleton instance;

  public static abstract class MessageObserver {
    public abstract void refresh();

    public void subscribe() {
      RefreshControlSingleton.getInstance().subscribe(this);
    }
    public void unsubscribe() {
      RefreshControlSingleton.getInstance().unsubscribe(this);
    }
  }

  public static synchronized RefreshControlSingleton getInstance() {
    if (instance == null) {
      instance = new RefreshControlSingleton();
    }
    return instance;
  }
  
  private final List<RefreshControlSingleton.MessageObserver> observers;
  
  /**
   * true if somewhere in the GUI an item is opened for editing. This is
   * erased by a refresh.
   */
  private boolean editingActive;

  public RefreshControlSingleton() {
    observers = new ArrayList<>();
  }
    
  private void subscribe(RefreshControlSingleton.MessageObserver observer) {
    unsubscribe(observer);  // avoid dups
    observers.add(observer);
  }
  
  private void unsubscribe(RefreshControlSingleton.MessageObserver observer) {
    Iterator<RefreshControlSingleton.MessageObserver> iterator = observers.iterator();
    while (iterator.hasNext()) {
      RefreshControlSingleton.MessageObserver current = iterator.next();
      if (current == observer) {
        iterator.remove();
      }
    }
  }

  
  public void broadcastRefresh() {
    System.out.println("Broadcast refresh");
    editingActive = false;
    for (RefreshControlSingleton.MessageObserver observer : observers) {
      observer.refresh();
    }
  }
  
  public boolean isEditingActive() {
    return editingActive;
  }
  
  public void setEditingActive(boolean editingActive) {
    this.editingActive = editingActive;
  }
}
