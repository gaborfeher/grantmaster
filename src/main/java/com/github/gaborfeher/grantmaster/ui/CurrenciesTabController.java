package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CurrenciesTabController extends ControllerBase {
  @FXML TextField codeEntry;

  @FXML TableView<Currency> currencyTable;

  @Override
  public void refresh() {
    List<Currency> list = DatabaseConnectionSingleton.getInstance().createQuery(
        "SELECT c FROM Currency c", Currency.class).getResultList();
    currencyTable.getItems().setAll(list);
  }

  public void addCurrencyButtonAction(ActionEvent e) {
    DatabaseConnectionSingleton connection = DatabaseConnectionSingleton.getInstance();
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
  }

  @Override
  protected EntityWrapper createNewEntity() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
