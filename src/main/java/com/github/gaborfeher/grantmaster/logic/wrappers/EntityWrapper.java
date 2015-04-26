package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

public abstract class EntityWrapper<T extends EntityBase> {
  protected T entity;
  
  public static enum State {
    EDITING_NEW,
    SAVED;
  }
  
  private State state;
  private boolean isSummary;
  private ControllerBase parent;
  
  public EntityWrapper(T entity) {
    this.entity = entity;
    state = State.SAVED;
    isSummary = false;
  }

  public boolean commitEdit(String property, Object val, Class<?> valueType) {
    if (!setProperty(property, val, valueType)) {
      return false;
    }
    if (state == EntityWrapper.State.EDITING_NEW) {
      // Nothing is to be done for newly created objects here. The user has to
      // click the create button to commit them.
      return true;
    }
    if (DatabaseSingleton.INSTANCE.transaction(
        (EntityManager em) -> save(em))) {
      refresh();
      return true;
    } else {
      return false;
    }
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

  public boolean setProperty(String name, Object value, Class<?> paramType) {
    Object entity = getEntity();
    try {
      String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      entity.getClass().getMethod(setterName, paramType).invoke(entity, value);
      return true;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(EntityWrapper.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
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
    parent.onRefresh();
  }

  public boolean save(EntityManager em) {
    entity = em.merge(getEntity());
    setState(State.SAVED);
    setEntity(entity);
    return true;
  }
  
  public void delete(EntityManager em) {
    EntityBase entityBase = getEntity();
    if (entityBase != null) {
      EntityBase entity = (EntityBase) em.find(entityBase.getClass(), entityBase.getId());
      em.remove(entity);
    }
  }
  
  public void discardEdits() {
    if (state == State.EDITING_NEW) {
      // This thing will just go away at next refresh.
    }
    parent.onRefresh();
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

  public Long getId() {
    if (getEntity() == null) {
      return null;
    } else {
      return getEntity().getId();
    }
  }
  
  public T getEntity() {
    return entity;
  }

  public void setEntity(T entity) {
    this.entity = entity;
  }
}
