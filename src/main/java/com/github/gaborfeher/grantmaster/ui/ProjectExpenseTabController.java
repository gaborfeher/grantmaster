package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;

public class ProjectExpenseTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TableView<ProjectExpenseWrapper> table;
  @FXML ExpenseTableController tableController;
  
  Project project;

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
    subscribe();
  }
  
  @Override
  public void refresh() {
    table.getItems().setAll(ProjectExpenseWrapper.getProjectExpenseList(project));
  }

  public void createProjectExpenseButtonAction(ActionEvent event) {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense, 0.0, 0.0);
    wrapper.setOriginalCurrency(project.getAccountCurrency());
    Utils.addNewEntityForEditing(wrapper, table.getItems());
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

}
