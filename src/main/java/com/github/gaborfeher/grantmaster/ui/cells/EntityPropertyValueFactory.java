/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author gabor
 */
public class EntityPropertyValueFactory<S extends EntityWrapper, T> implements Callback<TableColumn.CellDataFeatures<S,T>,ObservableValue<T>> {
  private String property;

  public EntityPropertyValueFactory() {
  }
  
  public EntityPropertyValueFactory(String property) {
    this.property = property;
  }
  
  public void setProperty(String property) {
    this.property = property;
  }
  
  public String getProperty() {
    return property;
  }
  
  
  @Override
  public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> p) {
    Object value = p.getValue().getProperty(property);
    return new ReadOnlyObjectWrapper<>(value == null ? null : (T) value);
  }
  
}