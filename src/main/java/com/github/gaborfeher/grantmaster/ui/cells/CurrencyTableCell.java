package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javax.persistence.EntityManager;

class CurrencyTableCell<S extends EntityWrapper> extends BetterChoiceBoxTableCell<S, Currency> {

  public CurrencyTableCell(String property) {
    super(new CurrencyStringConverter(), property, Currency.class);
  }

  @Override
  protected void refreshChoiceItems() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      getItems().setAll(em.createQuery("SELECT c FROM Currency c", Currency.class).getResultList());
      return true;
    });
  }

}
