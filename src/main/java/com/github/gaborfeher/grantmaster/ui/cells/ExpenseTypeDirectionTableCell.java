/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author gabor
 */
class ExpenseTypeDirectionTableCell<S> extends ChoiceBoxTableCell<S, ExpenseType.Direction> {
  String property;
  
  public ExpenseTypeDirectionTableCell(String property) {
    super(new ExpenseTypeDirectionStringConverter());
    this.property = property;
    getItems().add(ExpenseType.Direction.PAYMENT);
    getItems().add(ExpenseType.Direction.INCOME);
  }
    
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(ExpenseType.Direction val) {
    if (getEntityWrapper().setPropeprty(property, val)) {
      updateItem(val, false);
    }
  }

  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      super.startEdit();
    }
  }
  
  
  
  
}
