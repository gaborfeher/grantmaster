package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.time.LocalDate;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DateTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> {
  private String property;
  
  @Override  
  public TableCell<S, LocalDate> call(TableColumn<S, LocalDate> param) {  
    return new TextFieldTableCell(
        property,
        new DateStringConverter(),
        LocalDate.class);  
  }        

  /**
   * @return the property
   */
  public String getProperty() {
    return property;
  }

  /**
   * @param property the property to set
   */
  public void setProperty(String property) {
    this.property = property;
  }
  
}
