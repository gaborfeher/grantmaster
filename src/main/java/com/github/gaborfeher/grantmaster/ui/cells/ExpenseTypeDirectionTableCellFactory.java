package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ExpenseTypeDirectionTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, ExpenseType.Direction>, TableCell<S, ExpenseType.Direction>> {
  
  @Override
  public TableCell<S, ExpenseType.Direction> call(TableColumn<S, ExpenseType.Direction> p) {
    return new ExpenseTypeDirectionTableCell(property);  
  }
}