package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javax.persistence.EntityManager;

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
    if (getEntityWrapper().commitEdit(property, val, Currency.class)) {
      updateItem(val, false);
    }
  }     

  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      DatabaseSingleton.INSTANCE.query(new TransactionRunner() {
        @Override
        public boolean run(EntityManager em) {
          getItems().setAll(em.createQuery("SELECT c FROM Currency c", Currency.class).getResultList());
          return true;
        }
      });
      super.startEdit();
      
    }
  }
  
}
