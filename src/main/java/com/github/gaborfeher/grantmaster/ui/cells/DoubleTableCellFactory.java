package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DoubleTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, Double>, TableCell<S, Double>> {
  
  @Override  
  public TableCell<S, Double> call(TableColumn<S, Double> param) {  
    return new TextFieldTableCell(property, new DoubleStringConverter());  
  }        

}
