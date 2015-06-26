package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

public class ExpenseTableController implements Initializable {
  Project project;
  
  @FXML TableColumn<ProjectExpenseWrapper, Object> accountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectExpenseWrapper, Object> grantCurrencyAmountColumn;
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

  public void init(Project project) {
    this.project = project;
  }

  public void refresh() {
    if (project != null) {
      accountingCurrencyAmountColumn.setText(project.getAccountCurrency().toString());
      grantCurrencyAmountColumn.setText(project.getGrantCurrency().toString());    
    }
  }
 
}
