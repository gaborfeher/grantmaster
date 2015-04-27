package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.MyValidatorFactory;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
      parent.showBackendFailureDialog("EntityWrapper.commitEdit(): merge");
      return false;
    }
  }
  
  public boolean saveNew() {
    if (!validate()) {
      return false;
    }
    boolean success = DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> save(em));
    refresh();
    if (success == true) {
      return true;
    } else {
      parent.showBackendFailureDialog("EntityWrapper.saveNew(): merge");
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
      return entity.getClass().getMethod(getterName).invoke(entity);
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

  protected boolean save(EntityManager em) {
    entity = em.merge(entity);
    setState(State.SAVED);
    return true;
  }
  
  protected boolean validate() {
    Validator validator = MyValidatorFactory.SINGLE_INSTANCE.getValidator();
    Set<ConstraintViolation> constraintViolations = new HashSet<>();
    constraintViolations.addAll(validator.validate(entity));
    constraintViolations.addAll(validator.validate(this));
    if (constraintViolations.isEmpty()) {
      return true;
    } else {
      parent.showValidationFailureDialog(constraintViolations);
      return false;
    }
  }
  
  public void delete(EntityManager em) {
    if (entity != null) {
      entity = (T) em.find(entity.getClass(), entity.getId());
      em.remove(entity);
    }
  }
  
  public void discardEdits() {
    if (state == State.EDITING_NEW) {
      parent.discardNew();
    } else {
      parent.onRefresh();
    }
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
    if (entity == null) {
      return null;
    } else {
      return entity.getId();
    }
  }
  
  public T getEntity() {
    return entity;
  }

  public void setEntity(T entity) {
    this.entity = entity;
  }
}
