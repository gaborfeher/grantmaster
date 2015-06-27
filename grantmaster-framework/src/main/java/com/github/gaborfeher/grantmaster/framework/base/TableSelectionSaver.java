package com.github.gaborfeher.grantmaster.framework.base;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
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
