/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.Callback;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ExpenseTypeTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, ExpenseType>, TableCell<S, ExpenseType>> {
  private String property;
  
  @Override  
  public TableCell<S, ExpenseType> call(TableColumn<S, ExpenseType> param) {  
    return new ExpenseTypeTableCell(property);  
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
