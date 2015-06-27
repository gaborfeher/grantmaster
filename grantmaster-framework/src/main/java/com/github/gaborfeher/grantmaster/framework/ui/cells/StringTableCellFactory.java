package com.github.gaborfeher.grantmaster.framework.ui.cells;

import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class StringTableCellFactory<S extends EditableTableRowItem>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, String>, TableCell<S, String>> {
  
  @Override  
  public TableCell<S, String> call(TableColumn<S, String> param) {  
    return new TextFieldTableCell<>(
        property,
        new DefaultMultiStringConverter(),
        String.class);  
  }        

}
