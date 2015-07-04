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

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * Given a TableView, this class can store the position of its selected
 * cell, and later apply the selection to the table. This is useful when
 * updating the table content erases the selection. The table should be in cell
 * selection mode, and not row selection mode to work with this class.
 */
class TableSelectionSaver<T extends EntityWrapper> {
  TableView<T> table = null;
  TableColumn selectedColumn = null;
  Object selectedEntityId = null; // null means first line (new item)
  int selectedRow = 0;

  TableSelectionSaver(TableView<T> table) {
    this.table = table;
    if (table.getSelectionModel().getSelectedCells().size() > 0) {
      TablePosition selectedCell = table.getSelectionModel().getSelectedCells().get(0);
      selectedColumn = selectedCell.getTableColumn();
      selectedEntityId = table.getItems().get(selectedCell.getRow()).getId();
      selectedRow = selectedCell.getRow();
    }
  }

  void restore() {
    if (selectedColumn != null) {
      if (selectedEntityId != null) {
        int row = 0;
        while (row < table.getItems().size() && !selectedEntityId.equals(table.getItems().get(row).getId())) {
          row++;
        }
        if (row < table.getItems().size()) {
          selectedRow = row;
        }
      }
      if (selectedRow < table.getItems().size()) {
        table.getSelectionModel().clearAndSelect(selectedRow, selectedColumn);
      }
    } else {
      table.getSelectionModel().clearSelection();
    }
  }
  
}
