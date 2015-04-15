/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;

/**
 *
 * @author gabor
 */
class CurrencyTableCell<S extends EntityWrapper> extends ChoiceBoxTableCell<S, Currency> {
  String property;

  public CurrencyTableCell(String property) {
    super(new CurrencyStringConverter());
    this.property = property;
  }
  
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(Currency val) {
    if (getEntityWrapper().setPropeprty(property, val)) {
      updateItem(val, false);
    }
  }     

  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      getItems().setAll(DatabaseConnectionSingleton.getInstance().em().createQuery("SELECT c FROM Currency c", Currency.class).getResultList());
      super.startEdit();
    }
  }
  
}
