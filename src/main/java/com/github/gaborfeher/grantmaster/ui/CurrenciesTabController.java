package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CurrenciesTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TextField codeEntry;
  
  @FXML TableView<Currency> table;

  @Override
  public void refresh() {
    List<Currency> list = DatabaseConnectionSingleton.getInstance().em().createQuery("SELECT c FROM Currency c").getResultList();
    table.getItems().setAll(list);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    subscribe();
  }
  
  public void handleAddButtonAction(ActionEvent e) {
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
    RefreshControlSingleton.getInstance().broadcastRefresh();
  }
}
