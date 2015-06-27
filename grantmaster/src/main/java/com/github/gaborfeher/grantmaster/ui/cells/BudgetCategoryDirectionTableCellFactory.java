package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import com.github.gaborfeher.grantmaster.framework.ui.cells.PropertyTableCellFactoryBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BudgetCategoryDirectionTableCellFactory<S extends EditableTableRowItem>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, BudgetCategory.Direction>, TableCell<S, BudgetCategory.Direction>> {
  
  @Override
  public TableCell<S, BudgetCategory.Direction> call(TableColumn<S, BudgetCategory.Direction> p) {
    return new BudgetCategoryDirectionTableCell(property);  
  }
}