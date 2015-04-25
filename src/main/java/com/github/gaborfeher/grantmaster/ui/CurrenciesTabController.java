package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javax.persistence.EntityManager;

public class CurrenciesTabController extends ControllerBase {
  @FXML TextField codeEntry;

  @FXML TableView<Currency> currencyTable;

  @Override
  public void refreshContent() {
    DatabaseSingleton.INSTANCE.query(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        List<Currency> list = em.createQuery(
            "SELECT c FROM Currency c", Currency.class).getResultList();
        currencyTable.getItems().setAll(list);
        return true;
      }
    });
  }

  public void addCurrencyButtonAction(ActionEvent e) {
    DatabaseSingleton connection = DatabaseSingleton.INSTANCE;
    if (!connection.isConnected()) {
      return;
    }
    String code = codeEntry.getText();
    if (code == null || code.isEmpty()) {
      return;
    }
    Currency currency = new Currency();
    currency.setCode(code);
    connection.persist(currency);
    onRefresh();
  }

  @Override
  protected EntityWrapper createNewEntity() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
