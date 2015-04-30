package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EntityPropertyValueFactory<S extends EditableTableRowItem, T>
    implements Callback<TableColumn.CellDataFeatures<S,T>,ObservableValue<T>> {
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
