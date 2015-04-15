package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.sql.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DateTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, Date>, TableCell<S, Date>> {
  private String property;
  
  @Override  
  public TableCell<S, Date> call(TableColumn<S, Date> param) {  
    return new TextFieldTableCell(property, new DateStringConverter());  
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
