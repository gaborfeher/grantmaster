package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class StringTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, String>, TableCell<S, String>> {
  private String property;
  
  @Override  
  public TableCell<S, String> call(TableColumn<S, String> param) {  
    return new TextFieldTableCell<>(property, new DefaultStringConverter());  
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
