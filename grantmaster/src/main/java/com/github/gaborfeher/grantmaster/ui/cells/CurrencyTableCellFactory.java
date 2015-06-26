package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class CurrencyTableCellFactory<S extends EditableTableRowItem>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, Currency>, TableCell<S, Currency>> {
  
  @Override  
  public TableCell<S, Currency> call(TableColumn<S, Currency> param) {  
    return new CurrencyTableCell(property);  
  }        
  
}
