/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
class BudgetCategoryDirectionTableCell<S> extends ChoiceBoxTableCell<S, BudgetCategory.Direction> {
  String property;
  
  public BudgetCategoryDirectionTableCell(String property) {
    super(new BudgetCategoryDirectionStringConverter());
    this.property = property;
    getItems().add(BudgetCategory.Direction.PAYMENT);
    getItems().add(BudgetCategory.Direction.INCOME);
  }
    
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(BudgetCategory.Direction val) {
    if (getEntityWrapper().commitEdit(property, val)) {
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
