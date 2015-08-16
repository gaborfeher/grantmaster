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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.StringConverter;

/**
 * Adds some user friendliness and EntityWrapper-editing capabilities for
 * the standard Java FX choicebox table cell.
 */
public abstract class BetterChoiceBoxTableCell<S extends EditableTableRowItem, T>
    extends ChoiceBoxTableCell<S, T> {
  /**
   * The property of the edited EntityWrapper which will be updated on
   * commit.
   */
  private final String property;
  /**
   * The class of object being edited here, needed by the EntityWrapper.
   */
  private final Class valueClass;

  private class FocusChangeListener implements ChangeListener<Boolean> {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if (newValue) {
        startEdit();
      }
    }
  }

  public BetterChoiceBoxTableCell(
      StringConverter<T> stringConverter,
      String property,
      Class valueClass) {
    super(stringConverter);
    this.property = property;
    this.valueClass = valueClass;

    focusedProperty().addListener(new FocusChangeListener());
  }

  protected EditableTableRowItem getEntityWrapper() {
    return (EditableTableRowItem) getTableRow().getItem();
  }

  @Override
  public void commitEdit(T val) {
    if (getEntityWrapper() != null &&  // TODO(gaborfeher): Investigate why these are needed.
        getEntityWrapper().commitEdit(property, val, valueClass)) {
      updateItem(val, false);
    } else {
      cancelEdit();
    }
  }

  @Override
  public void startEdit() {
    if (getEntityWrapper() != null &&
        getEntityWrapper().canEdit()) {
      refreshChoiceItems();
      super.startEdit();
    }
  }

  /**
   * Retrieves and updates the list of choice items in the choice box.
   */
  protected abstract void refreshChoiceItems();

}
