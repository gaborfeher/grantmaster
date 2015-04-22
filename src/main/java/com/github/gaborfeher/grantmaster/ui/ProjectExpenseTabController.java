package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;

public class ProjectExpenseTabController extends ControllerBase<ProjectExpenseWrapper> {
  @FXML ExpenseTableController tableController;
  
  Project project;

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
  }
  
  @Override
  public void refresh() {
    table.getItems().setAll(ProjectExpenseWrapper.getProjectExpenseList(project, this));
  }

  @Override
  public ProjectExpenseWrapper createNewEntity() {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense, 0.0, 0.0);
    wrapper.setOriginalCurrency(project.getAccountCurrency());
    return wrapper;
  }

}
