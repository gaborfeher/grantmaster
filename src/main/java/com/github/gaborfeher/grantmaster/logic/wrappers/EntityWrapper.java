package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.TypedQuery;

public abstract class EntityWrapper {

  public static enum State {
    EDITING,
    EDITING_NEW,
    SAVED;
  }
  
  private State state;
  private boolean isSummary;
  private ControllerBase parent;
  
  public EntityWrapper() {
    state = State.SAVED;
    isSummary = false;
  }
  
  public State getState() {
    return state;
  }
  
  public void setState(State state) {
    this.state = state;
  }
  
  public boolean canEdit() {
    return state == State.EDITING || state == State.EDITING_NEW;
  }
  
  public boolean isFake() {
    return false;
  }
  
  public boolean setPropeprty(String name, Object value) {
    try {
      String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      getClass().getMethod(setterName, value.getClass()).invoke(this, value);
      return true;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(EntityWrapper.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }

  public void persist() {
    DatabaseConnectionSingleton.getInstance().persist(getEntity());
    parent.refresh();
  }
  
  public void delete() {
    DatabaseConnectionSingleton.getInstance().remove(getEntity());
    parent.refresh();
  }
  
  public void discardEdits() {
    if (state == State.EDITING) {
      DatabaseConnectionSingleton.getInstance().refresh(getEntity());
      setState(State.SAVED);
    } else if (state == State.EDITING_NEW) {
      // This thing will just go away at next refresh.
    }
    parent.refresh();
  }
  
  public boolean getIsSummary() {
    return isSummary;
  }
  
  public void setIsSummary(boolean isSummary) {
    this.isSummary = isSummary;
  }

  public ControllerBase getParent() {
    return parent;
  }
  
  public void setParent(ControllerBase parent) {
    this.parent = parent;
  }

  protected abstract Object getEntity();
  
  private static <T extends EntityWrapper> List<T> initEntityWrappers(List<T> list, ControllerBase parent) {
    for (T wrapper : list) {
      wrapper.setParent(parent);
    }
    return list;
  }
  
  static class MyQuery <T extends EntityWrapper> {
    TypedQuery<T> query;
    public MyQuery(String queryString, Class<T> resultClass) {
      this.query = DatabaseConnectionSingleton.getInstance().createQuery(queryString, resultClass);
    }
    
    public MyQuery setParameter(String paramName, Object paramValue) {
      query.setParameter(paramName, paramValue);
      return this;
    }
    
    public List<T> getResultList(ControllerBase parent) {
      return initEntityWrappers(query.getResultList(), parent);
    }
  }

  public static <T extends EntityWrapper> MyQuery<T> createQuery(String queryString, Class<T> resultClass) {
    return new MyQuery(queryString, resultClass);
  }
}
