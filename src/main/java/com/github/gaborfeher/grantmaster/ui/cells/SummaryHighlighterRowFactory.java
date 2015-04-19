package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.Collections;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class SummaryHighlighterRowFactory implements Callback<TableView<EntityWrapper>, TableRow<EntityWrapper>> {

  @Override
  public TableRow<EntityWrapper> call(TableView<EntityWrapper> p) {
    final TableRow<EntityWrapper> row = new TableRow<EntityWrapper>() {
      @Override
      protected void updateItem(EntityWrapper entityWrapper, boolean empty) {
        super.updateItem(entityWrapper, empty);
        if (!empty && entityWrapper != null && entityWrapper.isSummary()) {
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
