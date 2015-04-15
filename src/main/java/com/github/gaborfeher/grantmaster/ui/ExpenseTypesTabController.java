/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author gabor
 */
public class ExpenseTypesTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TableView<ExpenseTypeWrapper> table;

  public ExpenseTypesTabController() {
  }
  
  @Override
  public void refresh(RefreshMessage message) {
    table.getItems().setAll(ExpenseTypeWrapper.getExpenseTypes());
  }

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    RefreshControlSingleton.getInstance().subscribe(this);
  }  

  @FXML
  public void handleExpenseTypeAddButtonAction(ActionEvent event) {
    ExpenseType expenseType = new ExpenseType();
    ExpenseTypeWrapper wrapper = new ExpenseTypeWrapper(expenseType);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }

}
