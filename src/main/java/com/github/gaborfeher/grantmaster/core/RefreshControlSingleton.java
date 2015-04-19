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

  public RefreshControlSingleton() {
    observers = new ArrayList<>();
  }

  public void broadcastRefresh() {
    System.out.println("Broadcast refresh");
    for (RefreshControlSingleton.MessageObserver observer : observers) {
      observer.refresh();
    }
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

}
