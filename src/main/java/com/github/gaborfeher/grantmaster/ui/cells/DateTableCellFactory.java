package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import java.time.LocalDate;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DateTableCellFactory<S extends EditableTableRowItem>
    implements Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> {
  private String property;
  
  @Override  
  public TableCell<S, LocalDate> call(TableColumn<S, LocalDate> param) {  
    return new TextFieldTableCell(
        property,
        new DateStringConverter(),
        LocalDate.class);  
  }        

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }
  
}
