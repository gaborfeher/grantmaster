package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public abstract class EntityWrapper {
  public static enum State {
    EDITING,
    EDITING_NEW,
    SAVED;
  }
  
  private State state;
  
  public EntityWrapper() {
    state = State.SAVED;
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
    RefreshControlSingleton.getInstance().broadcastRefresh(null);
  }
  
  public void delete() {
    DatabaseConnectionSingleton.getInstance().remove(getEntity());
    RefreshControlSingleton.getInstance().broadcastRefresh(null);
  }
  
  public void discardEdits() {
    if (state == State.EDITING) {
      EntityManager em = DatabaseConnectionSingleton.getInstance().em();
      em.refresh(getEntity());
    } else if (state == State.EDITING_NEW) {
      // This thing will just go away at next refresh.
    }
    RefreshControlSingleton.getInstance().broadcastRefresh(null);  // TODO: narrower refresh
  }
  
  public boolean isSummary() {
    return false;
  }
  
  protected abstract Object getEntity();
}
