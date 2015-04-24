package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

public abstract class EntityWrapper {

  public boolean commitEdit(String property, Object val) {
    if (!setProperty(property, val)) {
      return false;
    }
    if (state == EntityWrapper.State.EDITING_NEW) {
      // Nothing is to be done for newly created objects here. The user has to
      // click the create button to commit them.
      return true;
    }
    return DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        return save(em);
      }
      @Override
      public void onSuccess() {
        refresh();
      }});
  }

  public static enum State {
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
    return !isSummary;
  }
  
  private boolean setEntityPropeprty(Object entity, String name, Object value) {
    try {
      String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      entity.getClass().getMethod(setterName, value.getClass()).invoke(entity, value);
      return true;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(EntityWrapper.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }
  
  public boolean setProperty(String name, Object value) {
    return setEntityPropeprty(getEntity(), name, value);
  }
  
  public Object getProperty(String name) {
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
    entity = em.merge(getEntity());
    setState(State.SAVED);
    setEntity(entity);
    return true;
  }
  
  public void delete() {
    EntityBase entityBase = getEntity();
    if (entityBase != null) {
      DatabaseConnectionSingleton.getInstance().remove(entityBase);
      parent.refresh();
    }
  }
  
  public void discardEdits() {
    if (state == State.EDITING_NEW) {
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

  
  public abstract EntityBase getEntity();
  protected abstract void setEntity(EntityBase entity);
}
