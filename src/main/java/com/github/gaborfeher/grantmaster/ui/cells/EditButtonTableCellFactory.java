package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EditButtonTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, EntityWrapper.State>, TableCell<S, EntityWrapper.State>> {

  @Override
  public TableCell<S, EntityWrapper.State> call(TableColumn<S, EntityWrapper.State> p) {
    return new EditButtonTableCell<S>();
  }
  
}
