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
package com.github.gaborfeher.grantmaster.framework.ui.cells;

import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
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
