package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ExpenseTypeTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, Object>, TableCell<S, Object>> {

  @Override  
  public TableCell<S, Object> call(TableColumn<S, Object> param) {  
    return new ExpenseTypeTableCell(property);  
  }        
}
