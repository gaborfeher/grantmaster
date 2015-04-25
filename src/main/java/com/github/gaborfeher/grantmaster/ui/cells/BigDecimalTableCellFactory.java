package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.math.BigDecimal;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BigDecimalTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> {
  
  @Override  
  public TableCell<S, BigDecimal> call(TableColumn<S, BigDecimal> param) {  
    return new TextFieldTableCell(
        property,
        new BigDecimalStringConverter(),
        BigDecimal.class);  
  }        

}
