/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class CurrencyTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, Currency>, TableCell<S, Currency>> {
  private String property;
  
  @Override  
  public TableCell<S, Currency> call(TableColumn<S, Currency> param) {  
    return new CurrencyTableCell(property);  
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
