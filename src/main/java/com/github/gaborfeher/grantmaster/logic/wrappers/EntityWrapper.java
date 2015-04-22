package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

public abstract class EntityWrapper {

  public static enum State {
    EDITING,
    EDITING_NEW,
    SAVED;
  }
  
  private State state;
  private boolean isSummary;
  private ControllerBase parent;
  
  protected final Map<String, Object> computedValues;
  protected final Map<String, Object> changedValues;
  
  public EntityWrapper() {
    state = State.SAVED;
    isSummary = false;
    changedValues = new HashMap<>();
    computedValues = new HashMap<>();
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
  
  public boolean setEntityPropeprty(Object entity, String name, Object value) {
    try {
      String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      entity.getClass().getMethod(setterName, value.getClass()).invoke(entity, value);
      return true;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(EntityWrapper.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }
  
  public boolean setPropeprty(String name, Object value) {
    changedValues.put(name, value);
    return true;
  }
  
  public Object getProperty(String name) {
    if (changedValues.containsKey(name)) {
      return changedValues.get(name);
    }
    if (computedValues.containsKey(name)) {
      return computedValues.get(name);
    }
    try {
      String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      return getEntity().getClass().getMethod(getterName).invoke(getEntity());
    } catch (NoSuchMethodException ex) {
      return null;
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(EntityWrapper.class.getName()).log(Level.SEVERE, "Property not found: " + name, ex);
      return null;
    }
  }
  
  public void refresh() {
    parent.refresh();
  }

  public boolean save(EntityManager em) {
    EntityBase entity;
    if (state == State.EDITING) {
      entity = (EntityBase) em.find(getEntity().getClass(), getEntity().getId());
    } else {
      entity = em.merge(getEntity());  // TODO(gaborfeher): build new entity from scratch here
    }
    for (Map.Entry<String, Object> entry : changedValues.entrySet()) {
      System.out.println(entry.getKey() + " <- " + entry.getValue());
      setEntityPropeprty(entity, entry.getKey(), entry.getValue());
    }
    changedValues.clear();
    setEntity(entity);
    return true;
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

  protected abstract EntityBase getEntity();
  protected abstract void setEntity(EntityBase entity);
}
