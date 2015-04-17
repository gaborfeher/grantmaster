package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class StringTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, String>, TableCell<S, String>> {
  
  @Override  
  public TableCell<S, String> call(TableColumn<S, String> param) {  
    return new TextFieldTableCell<>(property, new DefaultStringConverter());  
  }        

}
