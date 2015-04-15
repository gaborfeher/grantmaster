package com.github.gaborfeher.grantmaster.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RefreshControlSingleton {
  private static RefreshControlSingleton instance;

  public static abstract class MessageObserver {
    public abstract void refresh(RefreshMessage message);
    public void destroy(RefreshMessage message) {
      if (forMe(message)) {
        RefreshControlSingleton.getInstance().unsubscribe(this);
      }
    }
    public boolean forMe(RefreshMessage message) {
      return false;
    }
  }

  public static RefreshControlSingleton getInstance() {
    if (instance == null) {
      instance = new RefreshControlSingleton();
    }
    return instance;
  }
  
  private final List<RefreshControlSingleton.MessageObserver> observers;

  public RefreshControlSingleton() {
    observers = new ArrayList<>();
  }
  
  private List<RefreshControlSingleton.MessageObserver> getObserversCopy() {
    return new ArrayList<>(observers);
  }

  public void broadcastRefresh(RefreshMessage message) {
    System.out.println("Broadcast refresh");
    for (RefreshControlSingleton.MessageObserver observer : observers) {
      observer.refresh(message);
    }
  }
  
  public void broadcastDestroy(RefreshMessage message) {
    for (RefreshControlSingleton.MessageObserver observer : getObserversCopy()) {
      observer.destroy(message);
    }
  }
    
  public void subscribe(RefreshControlSingleton.MessageObserver observer) {
    unsubscribe(observer);  // avoid dups
    observers.add(observer);
  }
  
  public void unsubscribe(RefreshControlSingleton.MessageObserver observer) {
    Iterator<RefreshControlSingleton.MessageObserver> iterator = observers.iterator();
    while (iterator.hasNext()) {
      RefreshControlSingleton.MessageObserver current = iterator.next();
      if (current == observer) {
        iterator.remove();
      }
    }
  }

}
