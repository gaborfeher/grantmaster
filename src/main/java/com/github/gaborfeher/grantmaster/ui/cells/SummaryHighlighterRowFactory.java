package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import java.util.Collections;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class SummaryHighlighterRowFactory implements Callback<TableView<EditableTableRowItem>, TableRow<EditableTableRowItem>> {

  @Override
  public TableRow<EditableTableRowItem> call(TableView<EditableTableRowItem> p) {
    final TableRow<EditableTableRowItem> row = new TableRow<EditableTableRowItem>() {
      @Override
      protected void updateItem(EditableTableRowItem entityWrapper, boolean empty) {
        super.updateItem(entityWrapper, empty);
        if (!empty && entityWrapper != null && entityWrapper.getIsSummary()) {
          if (!getStyleClass().contains("summaryRow")) {
            getStyleClass().add("summaryRow");
          }
        } else {
          getStyleClass().removeAll(Collections.singleton("summaryRow"));
        }
      }
    };
    return row;
  }
  
}
