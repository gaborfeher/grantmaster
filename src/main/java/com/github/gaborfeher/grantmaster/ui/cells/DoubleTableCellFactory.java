package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DoubleTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, Double>, TableCell<S, Double>> {
  private String property;
  
  @Override  
  public TableCell<S, Double> call(TableColumn<S, Double> param) {  
    return new TextFieldTableCell(property, new DoubleStringConverter());  
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
