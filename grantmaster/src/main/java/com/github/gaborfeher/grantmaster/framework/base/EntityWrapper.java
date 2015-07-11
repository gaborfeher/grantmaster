/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.framework.base;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.ValidatorFactorySingleton;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.slf4j.LoggerFactory;

/**
 * Base class for all the entity wrappers. An entity wrapper wraps a JPA
 * Entity object. It provides functions for handling computed data fields, and
 * more importantly the wrapper bridges interaction between GUI controls and the
 * entities.
 * The JPA entities supported here have to be subclasses of EntityBase.
 * The GUI controls talking to EditableTableRowItem objects are the Java FX tab
 * controllers derived from ControllerBase, and the Java FX table cell
 * implementations in the ui.cells subpackage.
 * 
 * @param <T> Type of entity to wrap.
 */
public abstract class EntityWrapper<T extends EntityBase> implements EditableTableRowItem {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EntityWrapper.class);
  
  protected T entity;
  
  private RowEditState state;
  private boolean isSummary;
  private TablePageControllerBase parent;
  
  public EntityWrapper(T entity) {
    this.entity = entity;
    state = RowEditState.SAVED;
    isSummary = false;
  }

  public boolean commitEdit(String property, Object val, Class<?> valueType) {
    logger.info("commitEdit({}, {}, {})", property, val, valueType);
    if (Objects.equals(val, getProperty(property))) {
      return true;
    }
    
    if (!setProperty(property, val, valueType)) {
      return false;
    }
    if (state == RowEditState.EDITING_NEW) {
      // Nothing is to be done for newly created objects here. The user has to
      // click the create button to commit them.

      // Refresh the whole table. Currently this is only needed when the two
      // amount fields of a newly created expense are tied together and one
      // of them is edited, causing the other to refresh. It's simpler design
      // to do this update always, not only in that case.
      requestTableRefresh();
      return true;
    }
    if (!validate(false)) {
      // Validate but don't show error dialog. This is a hack to avoid showing
      // the dialog twich with TextFieldTableCell. The table cell needs to clean
      // up it's state before the dialog is shown.
      return false;
    }
    if (DatabaseSingleton.INSTANCE.transaction(
        (EntityManager em) -> save(em))) {
      requestTableRefresh();
      return true;
    } else {
      parent.showFailureDialog(
          "Dialog.Edit.FailureTitle", "Dialog.Edit.FailureContent");
      return false;
    }
  }
  
  public boolean saveNewInstance() {
    if (state != RowEditState.EDITING_NEW) {
      // TODO(gaborfeher): Log.
      return false;
    }
    if (!validate(true)) {
      requestTableRefresh();
      return false;
    }
    boolean success = DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> save(em));
    requestTableRefresh();
    if (success == true) {
      return true;
    } else {
      parent.showBackendFailureDialog("EntityWrapper.saveNew(): merge");
      return false;
    }
  }
  
  public RowEditState getState() {
    return state;
  }
  
  public void setState(RowEditState state) {
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
      logger.error("Cannot set property: EntityWrapper.setProperty({})", name, ex);
      return false;
    }
  }
  
  @Override
  public Object getProperty(String name) {
    try {
      String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      return entity.getClass().getMethod(getterName).invoke(entity);
    } catch (NoSuchMethodException ex) {
      return null;
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      logger.error("Property not found: EntityWrapper.getProperty({})", name, ex);
      return null;
    }
  }
  
  @Override
  public void requestTableRefresh() {
    if (parent != null) {
      parent.onRefresh();
    }
  }

  public boolean save(EntityManager em) {
    entity = em.merge(entity);
    setState(RowEditState.SAVED);
    return true;
  }
  
  private Set<ConstraintViolation> checkValidationConstraints() {
    Validator validator = ValidatorFactorySingleton.SINGLE_INSTANCE.getValidator();
    Set<ConstraintViolation> constraintViolations = new HashSet<>();
    constraintViolations.addAll(validator.validate(entity));
    constraintViolations.addAll(validator.validate(this));
    return constraintViolations;
  }
  
  @Override
  public boolean validate(boolean showDialog) {
    Set<ConstraintViolation> constraintViolations = checkValidationConstraints();
    if (constraintViolations.isEmpty()) {
      return true;
    } else {
      if (showDialog) {
        parent.showValidationFailureDialog(constraintViolations);
      }
      return false;
    }
  }
  
  public boolean delete(EntityManager em) {
    if (entity != null) {
      entity = (T) em.find(entity.getClass(), entity.getId());
      em.remove(entity);
    }
    return true;
  }
  
  public void discardEdits() {
    if (state == RowEditState.EDITING_NEW) {
      parent.discardNew();
    } else {
      parent.onRefresh();
    }
  }
  
  @Override
  public boolean getIsSummary() {
    return isSummary;
  }
  
  public void setIsSummary(boolean isSummary) {
    this.isSummary = isSummary;
  }

  @Override
  public void setParent(TablePageControllerBase parent) {
    this.parent = parent;
  }

  public TablePageControllerBase getParent() {
    return parent;
  }
  
  public Long getId() {
    if (entity == null) {
      return null;
    } else {
      return entity.getId();
    }
  }
  
  @Override
  public T getEntity() {
    return entity;
  }

  public void setEntity(T entity) {
    this.entity = entity;
  }
}
